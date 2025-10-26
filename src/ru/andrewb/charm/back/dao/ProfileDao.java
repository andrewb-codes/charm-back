package ru.andrewb.charm.back.dao;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import ru.andrewb.charm.back.dto.ProfileFilter;
import ru.andrewb.charm.back.dto.ProfileSelectQueryBuilder;
import ru.andrewb.charm.back.dto.ProfileUpdateQueryBuilder;
import ru.andrewb.charm.back.dto.Query;
import ru.andrewb.charm.back.model.Gender;
import ru.andrewb.charm.back.model.Profile;
import ru.andrewb.charm.back.model.Role;
import ru.andrewb.charm.back.model.Status;
import ru.andrewb.charm.back.utils.ConnectionManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
                    return Optional.of(mapToProfile(rs));
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
                    return Optional.of(mapToProfile(rs));
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
                .addStatusFilter(filter.getStatus())
                .addLowerAgeBound(filter.getLowerAgeBound())
                .addGreaterAndEqualAgeBound(filter.getGreaterAndEqualAgeBound())
                .build();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = ConnectionManager.getPreparedStmt(conn, query)) {
            ps.setFetchSize(FETCH_SIZE);
            ps.setMaxRows(MAX_ROWS);
            ps.setQueryTimeout(QUERY_TIMEOUT);
            ResultSet rs = ps.executeQuery();

            List<Profile> profiles = new ArrayList<>();
            while (rs.next()) {
                profiles.add(mapToProfile(rs));
            }
            return profiles;
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

    private Profile mapToProfile(ResultSet rs) throws SQLException {
        Profile p = new Profile();
        p.setId(rs.getLong("id"));
        p.setEmail(rs.getString("email"));
        p.setPassword(rs.getString("password"));
        p.setName(rs.getString("name"));
        p.setSurname(rs.getString("surname"));

        Date birthdate = rs.getDate("birthdate");
        if (birthdate != null) {
            p.setBirthdate(birthdate.toLocalDate());
        }
        p.setAbout(rs.getString("about"));
        p.setPhoto(rs.getString("photo"));

        String gender = rs.getString("gender");
        if (gender != null) {
            p.setGender(Gender.valueOf(gender.toUpperCase(Locale.ROOT)));
        }
        String status = rs.getString("status");
        if (status != null) {
            p.setStatus(Status.valueOf(status.toUpperCase(Locale.ROOT)));
        }
        String role = rs.getString("role");
        if (role != null) {
            p.setRole(Role.valueOf(role.toUpperCase(Locale.ROOT)));
        }
        return p;
    }
}
