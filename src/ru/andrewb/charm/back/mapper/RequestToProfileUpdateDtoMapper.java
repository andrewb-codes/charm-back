package ru.andrewb.charm.back.mapper;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import ru.andrewb.charm.back.dto.ProfileUpdateDto;
import ru.andrewb.charm.back.model.Gender;
import ru.andrewb.charm.back.model.Status;
import ru.andrewb.charm.back.model.exception.BadRequestException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Locale;

import static ru.andrewb.charm.back.utils.Strings.stripToNull;

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

    @SneakyThrows
    @Override
    public ProfileUpdateDto map(HttpServletRequest req, ProfileUpdateDto dto) {
        dto.setName(stripToNull(req.getParameter("name")));
        dto.setSurname(stripToNull(req.getParameter("surname")));
        dto.setAbout(stripToNull(req.getParameter("about")));

        String bd = stripToNull(req.getParameter("birthDate"));
        if (bd != null) {
            try {
                dto.setBirthDate(LocalDate.parse(bd));
            } catch (DateTimeParseException e) {
                throw new BadRequestException("birthDate must be yyyy-MM-dd");
            }
        }

        String gp = stripToNull(req.getParameter("gender"));
        if (gp != null) {
            try {
                dto.setGender(Gender.valueOf(gp.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("gender must be one of " + Arrays.toString(Gender.values()));
            }
        }

        String st = stripToNull(req.getParameter("status"));
        if (st != null) {
            try {
                dto.setStatus(Status.valueOf(st.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("status must be one of " + Arrays.toString(Status.values()));
            }
        }

        String ct = req.getContentType();
        if (ct != null && ct.toLowerCase().startsWith("multipart/")) {
            try {
                dto.setPhoto(req.getPart("photo"));
            } catch (Exception ignore) {

            }
        }

        return dto;
    }
}
