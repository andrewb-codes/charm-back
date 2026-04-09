package ru.andrewb.charm.back.mapper;

import org.springframework.stereotype.Component;
import ru.andrewb.charm.back.dto.ProfileSimpleDto;
import ru.andrewb.charm.back.model.exception.MappingException;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class ResultSetToProfileSimpleDtoMapper implements Mapper<ResultSet, ProfileSimpleDto> {

    @Override
    public ProfileSimpleDto map(ResultSet rs) {
        return map(rs, new ProfileSimpleDto());
    }

    @Override
    public ProfileSimpleDto map(ResultSet rs, ProfileSimpleDto dto) {
        try {
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
        } catch (SQLException e) {
            throw new MappingException("error.mapper.resultSetToProfileSimpleDto", e);
        }
    }
}