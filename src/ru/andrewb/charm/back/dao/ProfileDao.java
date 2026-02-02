package ru.andrewb.charm.back.dao;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.dto.*;
import ru.andrewb.charm.back.mapper.ResultSetToProfileMapper;
import ru.andrewb.charm.back.mapper.ResultSetToProfileSimpleDtoMapper;
import ru.andrewb.charm.back.model.Profile;
import ru.andrewb.charm.back.model.Status;
import ru.andrewb.charm.back.model.exception.OptimisticLockException;
import ru.andrewb.charm.back.utils.ConnectionManager;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import static ru.andrewb.charm.back.utils.ConnectionManager.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileDao {
    //language=POSTGRES-PSQL
    private static final String SQL_INSERT =
            "INSERT INTO profile(email, password) VALUES (?, ?) RETURNING id";
    //language=POSTGRES-PSQL
    public static final String SQL_UPDATE_STATUSES =
            "UPDATE profile SET status = ?, version = version + 1 WHERE id = ? AND version = ?";
    //language=POSTGRES-PSQL
    private static final String SQL_DELETE_BY_ID =
            "DELETE FROM profile WHERE id = ?";
    //language=POSTGRES-PSQL
    private static final String SQL_EXISTS_EMAIL =
            "SELECT 1 FROM profile WHERE email = ?";
    //language=POSTGRES-PSQL
    private static final String SQL_EXISTS_EMAIL_EXCLUDING_ID =
            "SELECT 1 FROM profile WHERE email = ? AND id <> ?";
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

    private static final ProfileDao INSTANCE = new ProfileDao();

    private static final ResultSetToProfileMapper rsToProfileMapper = ResultSetToProfileMapper.getInstance();
    private static final ResultSetToProfileSimpleDtoMapper rsToProfileSimpleDtoMapper = ResultSetToProfileSimpleDtoMapper.getInstance();

    public static ProfileDao getInstance() {
        return INSTANCE;
    }

    public Profile save(Profile profile) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_INSERT)) {
            ps.setString(1, profile.getEmail());
            ps.setString(2, profile.getPassword());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    profile.setId(rs.getLong(1));
                }
            }
            return profile;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Profile> findById(Long id) {
        Query query = new ProfileSelectQueryBuilder().addIdFilter(id).build();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = ConnectionManager.getPreparedStmt(conn, query)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rsToProfileMapper.map(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Profile> findByEmail(String email) {
        Query query = new ProfileSelectQueryBuilder().addEmailFilter(email).build();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = ConnectionManager.getPreparedStmt(conn, query)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rsToProfileMapper.map(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Profile> findAll(ProfileFilter filter) {
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

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = ConnectionManager.getPreparedStmt(conn, query)) {
            try (ResultSet rs = ps.executeQuery()) {
                List<Profile> profiles = new ArrayList<>();
                while (rs.next()) {
                    profiles.add(rsToProfileMapper.map(rs));
                }
                return profiles;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Profile> findMatches(Long id, int limit, int offset) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_MATCHES)) {
            ps.setLong(1, id);
            ps.setLong(2, id);
            ps.setLong(3, id);
            ps.setInt(4, limit);
            ps.setInt(5, offset);

            try (ResultSet rs = ps.executeQuery()) {
                List<Profile> profiles = new ArrayList<>();
                while (rs.next()) {
                    profiles.add(rsToProfileMapper.map(rs));
                }
                return profiles;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = ConnectionManager.getPreparedStmt(conn, query)) {
            int updated =  ps.executeUpdate();
            if (updated == 0) {
                throw new OptimisticLockException("error.optimistic-lock");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateStatuses(List<ProfileUpdateStatusDto> dtoList) {
        Connection conn = null;
        try {
            conn = ConnectionManager.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement ps = conn.prepareStatement(SQL_UPDATE_STATUSES)) {
                for (ProfileUpdateStatusDto dto : dtoList) {
                    ps.setString(1, dto.getStatus().toString());
                    ps.setLong(2, dto.getId());
                    ps.setInt(3, dto.getVersion());
                    ps.addBatch();
                }
                int[] res = ps.executeBatch();

                List<Long> conflicted = new ArrayList<>();
                for (int i = 0; i < res.length; i++) {
                    if (res[i] == 0 || res[i] == Statement.EXECUTE_FAILED) {
                        conflicted.add(dtoList.get(i).getId());
                    }
                }
                if (!conflicted.isEmpty()) {
                    conn.rollback();
                    throw new OptimisticLockException("error.optimistic-lock");
                }
                conn.commit();
            }
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ignored) {}
            }
            throw new RuntimeException(e);
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public boolean delete(Long id) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_DELETE_BY_ID)) {
            ps.setLong(1, id);
            int deleted = ps.executeUpdate();
            return deleted > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean existsEmail(String email, Long excludeId) {
        final String sql = (excludeId == null) ? SQL_EXISTS_EMAIL : SQL_EXISTS_EMAIL_EXCLUDING_ID;

        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            if (excludeId != null) {
                ps.setLong(2, excludeId);
            }
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void ping() {
        try (Connection conn = ConnectionManager.getConnection()) {
            // ok: connection successful
        } catch (SQLException e) {
            throw new IllegalStateException("DB connection failed (ping)", e);
        }
    }

    public Queue<ProfileSimpleDto> findSuitableForUser(Long userId, int limit) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_SUITABLE)) {
            ps.setObject(1, userId);
            ps.setInt(2, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                Queue<ProfileSimpleDto> profiles = new LinkedList<>();
                while (rs.next()) {
                    profiles.offer(rsToProfileSimpleDtoMapper.map(rs));
                }
                return profiles;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
