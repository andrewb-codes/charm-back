package ru.andrewb.charm.back.dao;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import ru.andrewb.charm.back.model.Gender;
import ru.andrewb.charm.back.model.Profile;
import ru.andrewb.charm.back.model.Role;
import ru.andrewb.charm.back.model.Status;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileDao {

    private static final ProfileDao INSTANCE = new ProfileDao();

    private static final String URL = "jdbc:postgresql://localhost:5434/charm_repository";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1546";

    // SQL
    private static final String SQL_INSERT =
            "insert into profile(email, password) values (?, ?) returning id";
    private static final String SQL_FIND_BY_ID =
            "select * from profile where id = ?";
    private static final String SQL_FIND_BY_EMAIL =
            "select * from profile where email = ?";
    private static final String SQL_FIND_ALL =
            "select * from profile order by id";
    private static final String SQL_DELETE_BY_ID =
            "delete from profile where id = ?";
    private static final String SQL_EXISTS_EMAIL =
            "select 1 from profile where email = ?";
    private static final String SQL_EXISTS_EMAIL_EXCLUDING_ID =
            "select 1 from profile where email = ? and id <> ?";


    @SneakyThrows
    public static ProfileDao getInstance() {
        Class.forName("org.postgresql.Driver");
        return INSTANCE;
    }

    public Profile save(Profile profile) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
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
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_ID)) {
            ps.setLong(1, id);
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
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_BY_EMAIL)) {
            ps.setString(1, email);
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

    public List<Profile> findAll() {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(SQL_FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
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
        StringBuilder sql = new StringBuilder("update profile set email = ?, password = ?");
        List<Object> args = new ArrayList<>();
        args.add(profile.getEmail());
        args.add(profile.getPassword());

        if (profile.getName() != null) {
            sql.append(", name = ?");
            args.add(profile.getName());
        }
        if (profile.getSurname() != null) {
            sql.append(", surname = ?");
            args.add(profile.getSurname());
        }
        if (profile.getBirthdate() != null) {
            sql.append(", birthdate = ?");
            args.add(Date.valueOf(profile.getBirthdate()));
        }
        if (profile.getAbout() != null) {
            sql.append(", about = ?");
            args.add(profile.getAbout());
        }
        if (profile.getGender() != null) {
            sql.append(", gender = ?");
            args.add(profile.getGender().name());
        }
        if (profile.getStatus() != null) {
            sql.append(", status = ?");
            args.add(profile.getStatus().name());
        }
        if (profile.getPhoto() != null) {
            sql.append(", photo = ?");
            args.add(profile.getPhoto());
        }

        sql.append(" where id = ?");
        args.add(profile.getId());

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < args.size(); i++) {
                ps.setObject(i + 1, args.get(i));
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean delete(Long id) {
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
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

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
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
