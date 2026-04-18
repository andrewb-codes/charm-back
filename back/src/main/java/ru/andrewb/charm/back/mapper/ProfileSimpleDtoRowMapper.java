package ru.andrewb.charm.back.mapper;

import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import ru.andrewb.charm.back.dto.ProfileSimpleDto;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class ProfileSimpleDtoRowMapper implements RowMapper<ProfileSimpleDto> {

    @Override
    public ProfileSimpleDto mapRow(ResultSet rs, int rowNum) throws SQLException {
        ProfileSimpleDto dto = new ProfileSimpleDto();
        dto.setId(rs.getLong("id"));
        dto.setName(rs.getString("name"));
        dto.setSurname(rs.getString("surname"));
        Date birthdate = rs.getDate("birthdate");
        if (birthdate != null) {
            dto.setAge(Math.toIntExact(ChronoUnit.YEARS.between(birthdate.toLocalDate(), LocalDate.now())));
        }
        dto.setAbout(rs.getString("about"));
        dto.setPhoto(rs.getString("photo"));
        return dto;
    }
}
