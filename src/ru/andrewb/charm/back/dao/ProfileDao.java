package ru.andrewb.charm.back.dao;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import ru.andrewb.charm.back.dto.*;
import ru.andrewb.charm.back.mapper.ResultSetToProfileMapper;
import ru.andrewb.charm.back.mapper.ResultSetToProfileSimpleDtoMapper;
import ru.andrewb.charm.back.model.Profile;
import ru.andrewb.charm.back.utils.ConnectionManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static ru.andrewb.charm.back.utils.ConnectionManager.*;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileDao {

    private static final ProfileDao INSTANCE = new ProfileDao();

    private static final String SQL_INSERT =
            "insert into profile(email, password) values (?, ?) returning id";
    private static final String SQL_DELETE_BY_ID =
            "delete from profile where id = ?";
    private static final String SQL_EXISTS_EMAIL =
            "select 1 from profile where email = ?";
    private static final String SQL_EXISTS_EMAIL_EXCLUDING_ID =
            "select 1 from profile where email = ? and id <> ?";
    private static final String SQL_FIND_SUITABLE = """
            WITH cup AS (
                SELECT id, gender, birthdate
                FROM profile
                WHERE id = ?
            )
            SELECT p.id, p.name, p.surname, p.birthdate, p.about, p.photo
            FROM profile p
            JOIN cup ON true
            LEFT JOIN profile_like l
              ON l.from_profile = cup.id    -- голосовал ТЕКУЩИЙ пользователь
             AND l.to_profile   = p.id      -- за эту анкету
            WHERE l.from_profile IS NULL    -- текущий пользователь ещё не голосовал за p
              AND p.id <> cup.id
              AND p.status = 'ACTIVE'
              -- пол: фильтр включается только если он известен у текущего юзера
              AND (cup.gender IS NULL OR (p.gender IS NOT NULL AND p.gender <> cup.gender))
              -- возрастное окно ±5 лет: включается только если известна ДР у текущего юзера
              AND (cup.birthdate IS NULL
                   OR p.birthdate BETWEEN (cup.birthdate - INTERVAL '5 years')
                                    AND   (cup.birthdate + INTERVAL '5 years'))
            ORDER BY RANDOM()
            LIMIT ?
            """;

    private static final ResultSetToProfileMapper rsToProfileMapper = ResultSetToProfileMapper.getInstance();
    private static final ResultSetToProfileSimpleDtoMapper rsToProfileSimpleDtoMapper = ResultSetToProfileSimpleDtoMapper.getInstance();

    @SneakyThrows
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
            ps.setFetchSize(FETCH_SIZE);
            ps.setMaxRows(MAX_ROWS);
            ps.setQueryTimeout(QUERY_TIMEOUT);

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
                .build(profile.getId());
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = ConnectionManager.getPreparedStmt(conn, query)) {
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
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

    public List<ProfileSimpleDto> findSuitableForUser(Long userId, int limit) {
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_SUITABLE)) {
            ps.setObject(1, userId);
            ps.setInt(2, Math.max(1, limit));
            try (ResultSet rs = ps.executeQuery()) {
                List<ProfileSimpleDto> profiles = new ArrayList<>();
                while (rs.next()) {
                    profiles.add(rsToProfileSimpleDtoMapper.map(rs));
                }
                return profiles;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
