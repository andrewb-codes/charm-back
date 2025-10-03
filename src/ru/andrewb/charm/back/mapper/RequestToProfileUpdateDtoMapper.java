package ru.andrewb.charm.back.mapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.dto.ProfileUpdateDto;
import ru.andrewb.charm.back.model.Gender;
import ru.andrewb.charm.back.model.Status;
import ru.andrewb.charm.back.model.exception.BadRequestException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Locale;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RequestToProfileUpdateDtoMapper implements Mapper<HttpServletRequest, ProfileUpdateDto> {

    private static final RequestToProfileUpdateDtoMapper INSTANCE = new RequestToProfileUpdateDtoMapper();

    public static RequestToProfileUpdateDtoMapper getInstance() {
        return INSTANCE;
    }

    @Override
    public ProfileUpdateDto map(HttpServletRequest req) {
        return map(req, new ProfileUpdateDto());
    }

    @Override
    public ProfileUpdateDto map(HttpServletRequest req, ProfileUpdateDto dto) {
        String email = req.getParameter("email");
        if (email != null && !email.isBlank()) {
            dto.setEmail(email.trim());
        }
        dto.setName(trimToNull(req.getParameter("name")));
        dto.setSurname(trimToNull(req.getParameter("surname")));
        dto.setAbout(trimToNull(req.getParameter("about")));

        String bd = req.getParameter("birthDate");
        if (bd != null && !bd.isBlank()) {
            try {
                dto.setBirthDate(LocalDate.parse(bd));
            } catch (DateTimeParseException e) {
                throw new BadRequestException("birthDate must be yyyy-MM-dd");
            }
        }

        String gp = req.getParameter("gender");
        if (gp != null && !gp.isBlank()) {
            try {
                dto.setGender(Gender.valueOf(gp.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("gender must be one of " + Arrays.toString(Gender.values()));
            }
        }

        String st = req.getParameter("status");
        if (st != null && !st.isBlank()) {
            try {
                dto.setStatus(Status.valueOf(st.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("status must be one of " + Arrays.toString(Status.values()));
            }
        }
        return dto;
    }


    private static String trimToNull(String s) {
        if (s == null) return null;
        s = s.trim();
        return s.isEmpty() ? null : s;
    }
}
