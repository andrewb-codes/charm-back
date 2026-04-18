package ru.andrewb.charm.back.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.andrewb.charm.back.model.Gender;
import ru.andrewb.charm.back.model.Profile;
import ru.andrewb.charm.back.model.Role;
import ru.andrewb.charm.back.model.Status;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;

@Component
public class ProfileRowMapper implements RowMapper<Profile> {

    @Override
    public Profile mapRow(ResultSet rs, int rowNum) throws SQLException {
        Profile profile = new Profile();
        profile.setId(rs.getLong("id"));
        profile.setVersion(rs.getInt("version"));
        profile.setEmail(rs.getString("email"));
        profile.setPassword(rs.getString("password"));
        profile.setName(rs.getString("name"));
        profile.setSurname(rs.getString("surname"));
        Date birthdate = rs.getDate("birthdate");
        if (birthdate != null) {
            profile.setBirthdate(birthdate.toLocalDate());
        }
        profile.setAbout(rs.getString("about"));
        profile.setPhoto(rs.getString("photo"));

        String gender = rs.getString("gender");
        if (gender != null) {
            profile.setGender(Gender.valueOf(gender.toUpperCase(Locale.ROOT)));
        }
        String status = rs.getString("status");
        if (status != null) {
            profile.setStatus(Status.valueOf(status.toUpperCase(Locale.ROOT)));
        }
        String role = rs.getString("role");
        if (role != null) {
            profile.setRole(Role.valueOf(role.toUpperCase(Locale.ROOT)));
        }
        return profile;
    }
}
