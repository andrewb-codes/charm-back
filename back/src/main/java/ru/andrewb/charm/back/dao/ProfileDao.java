package ru.andrewb.charm.back.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.andrewb.charm.back.dto.*;
import ru.andrewb.charm.back.mapper.ProfileRowMapper;
import ru.andrewb.charm.back.mapper.ProfileSimpleDtoRowMapper;
import ru.andrewb.charm.back.model.Profile;
import ru.andrewb.charm.back.model.exception.OptimisticLockException;
import ru.andrewb.charm.back.service.command.ProfileUpdateStatusCommand;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;

@Repository
public class ProfileDao {

    private final JdbcTemplate jdbcTemplate;
    private final ProfileRowMapper profileRowMapper;
    private final ProfileSimpleDtoRowMapper profileSimpleDtoRowMapper;

    public ProfileDao(
            JdbcTemplate jdbcTemplate,
            ProfileRowMapper profileRowMapper,
            ProfileSimpleDtoRowMapper profileSimpleDtoRowMapper
    ) {
        this.jdbcTemplate = jdbcTemplate;
        this.profileRowMapper = profileRowMapper;
        this.profileSimpleDtoRowMapper = profileSimpleDtoRowMapper;
    }

    //language=POSTGRES-PSQL
    private static final String SQL_INSERT =
            "INSERT INTO profile(email, password) VALUES (?, ?) RETURNING id";
    //language=POSTGRES-PSQL
    private static final String SQL_UPDATE_STATUSES =
            "UPDATE profile SET status = ?, version = version + 1 WHERE id = ? AND version = ?";
    //language=POSTGRES-PSQL
    private static final String SQL_DELETE_BY_ID =
            "DELETE FROM profile WHERE id = ?";
    //language=POSTGRES-PSQL
    private static final String SQL_EXISTS_EMAIL =
            "SELECT COUNT(*) FROM profile WHERE email = ?";
    //language=POSTGRES-PSQL
    private static final String SQL_EXISTS_EMAIL_EXCLUDING_ID =
            "SELECT COUNT(*) FROM profile WHERE email = ? AND id <> ?";
    //language=POSTGRES-PSQL
    private static final String SQL_FIND_SUITABLE = """
            WITH me AS (
                SELECT id, gender, birthdate
                FROM profile
                WHERE id = ?
            )
            SELECT p.id, p."name", p.surname, p.birthdate, p.about, p.photo
            FROM profile p
            CROSS JOIN me
            LEFT JOIN profile_like l
              ON l.a_profile = LEAST(me.id, p.id)
             AND l.b_profile = GREATEST(me.id, p.id)
            WHERE p.id <> me.id
              AND p.status = 'ACTIVE'
              -- текущий (me) еще не голосовал за p
              AND (
                   l.a_profile IS NULL
                OR (me.id = l.a_profile AND l.liked_a IS NULL)
                OR (me.id = l.b_profile AND l.liked_b IS NULL)
              )
              -- пол (если известен пол у текущего)
              AND (me.gender IS NULL OR (p.gender IS NOT NULL AND p.gender <> me.gender))
              -- возрастное окно +- 5 лет (если известно ДР у текущего)
              AND (me.birthdate IS NULL
                    OR p.birthdate BETWEEN (me.birthdate - INTERVAL '5 years')
                                       AND (me.birthdate + INTERVAL '5 years'))
            ORDER BY RANDOM()
            LIMIT ?
            """;
    // language=POSTGRES-PSQL
    public static final String SQL_FIND_MATCHES = """
            SELECT p.*
            FROM profile_like l
            JOIN profile p
              ON p.id = CASE WHEN l.a_profile = ? THEN l.b_profile ELSE l.a_profile END
            WHERE (l.a_profile = ? OR l.b_profile = ?)
              AND l.liked_a IS TRUE AND l.liked_b IS TRUE
            ORDER BY l.updated_at DESC
            LIMIT ? OFFSET ?
            """;


    public Profile save(Profile profile) {
        Long id = jdbcTemplate.queryForObject(
                SQL_INSERT,
                Long.class,
                profile.getEmail(),
                profile.getPassword()
        );

        profile.setId(id);
        return profile;
    }

    public Optional<Profile> findById(Long id) {
        Query query = new ProfileSelectQueryBuilder().addIdFilter(id).build();

        List<Profile> result = jdbcTemplate.query(
                query.sql(),
                profileRowMapper,
                query.args().toArray()
        );

        return result.stream().findFirst();
    }

    public Optional<Profile> findByEmail(String email) {
        Query query = new ProfileSelectQueryBuilder().addEmailFilter(email).build();

        List<Profile> result = jdbcTemplate.query(
                query.sql(),
                profileRowMapper,
                query.args().toArray()
        );

        return result.stream().findFirst();
    }

    public List<Profile> findAll(ProfilesFilter filter) {
        Query query = new ProfileSelectQueryBuilder()
                .addEmailStartsWithFilter(filter.getEmailStartsWith())
                .addNameStartsWithFilter(filter.getNameStartsWith())
                .addSurnameStartsWithFilter(filter.getSurnameStartsWith())
                .addLowerAgeBound(filter.getLowerAgeBound())
                .addGreaterAndEqualAgeBound(filter.getGreaterAndEqualAgeBound())
                .addRoleFilter(filter.getRole())
                .addStatusFilter(filter.getStatus())
                .orderBy(filter.getSortBy(), filter.getSortOrder())
                .pageAndPageSize(filter.getPage(), filter.getPageSize())
                .build();

        return jdbcTemplate.query(
                con -> {
                    PreparedStatement ps = con.prepareStatement(query.sql());
                    Object[] args = query.args().toArray();
                    for (int i = 0; i < args.length; i++) {
                        ps.setObject(i + 1, args[i]);
                    }
                    return ps;
                },
                profileRowMapper
        );
    }

    public List<Profile> findMatches(Long id, int limit, int offset) {
        return jdbcTemplate.query(
                SQL_FIND_MATCHES,
                profileRowMapper,
                id, id, id, limit, offset
        );
    }

    public void update(Profile profile) {
        Query query = new ProfileUpdateQueryBuilder()
                .addEmail(profile.getEmail())
                .addPassword(profile.getPassword())
                .addName(profile.getName())
                .addSurname(profile.getSurname())
                .addBirthdate(profile.getBirthdate())
                .addAbout(profile.getAbout())
                .addGender(profile.getGender())
                .addStatus(profile.getStatus())
                .addPhoto(profile.getPhoto())
                .build(profile.getId(), profile.getVersion());

        int updated = jdbcTemplate.update(
                query.sql(),
                query.args().toArray()
        );

        if (updated == 0) {
            throw new OptimisticLockException("error.optimistic-lock");
        }
    }

    public void updateStatuses(List<ProfileUpdateStatusCommand> dtoList) {
        if (dtoList.isEmpty()) return;

        List<Object[]> batchArgs = dtoList.stream()
                .map(dto -> new Object[] {
                        dto.getStatus().toString(),
                        dto.getId(),
                        dto.getVersion()
                })
                .toList();

        int[] result = jdbcTemplate.batchUpdate(
                SQL_UPDATE_STATUSES,
                batchArgs
        );

        for (int updateCount : result) {
            if (updateCount == 0 || updateCount == Statement.EXECUTE_FAILED) {
                throw new OptimisticLockException("error.optimistic-lock");
            }
        }
    }

    public boolean delete(Long id) {
        int deleted = jdbcTemplate.update(SQL_DELETE_BY_ID, id);
        return deleted > 0;
    }

    public boolean existsEmail(String email, Long excludeId) {
        final String sql = (excludeId == null) ? SQL_EXISTS_EMAIL : SQL_EXISTS_EMAIL_EXCLUDING_ID;

        Integer count = (excludeId == null)
                ? jdbcTemplate.queryForObject(sql, Integer.class, email)
                : jdbcTemplate.queryForObject(sql, Integer.class, email, excludeId);

        return count != null && count > 0;
    }

    public Queue<ProfileSimpleDto> findSuitableForUser(Long userId, int limit) {
        List<ProfileSimpleDto> result = jdbcTemplate.query(
                SQL_FIND_SUITABLE,
                profileSimpleDtoRowMapper,
                userId,
                Math.max(1, limit)
        );

        return new LinkedList<>(result);
    }
}
