package ru.andrewb.charm.back.mapper;

import lombok.SneakyThrows;
import ru.andrewb.charm.back.dto.ProfileSimpleDto;

import java.sql.Date;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class ResultSetToProfileSimpleDtoMapper implements Mapper<ResultSet, ProfileSimpleDto> {

    private static final ResultSetToProfileSimpleDtoMapper INSTANCE = new ResultSetToProfileSimpleDtoMapper();

    public static ResultSetToProfileSimpleDtoMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public ProfileSimpleDto map(ResultSet rs) {
        return map(rs, new ProfileSimpleDto());
    }

    @Override
    @SneakyThrows
    public ProfileSimpleDto map(ResultSet rs, ProfileSimpleDto dto) {
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