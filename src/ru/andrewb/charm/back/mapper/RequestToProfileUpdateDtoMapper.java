package ru.andrewb.charm.back.mapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.andrewb.charm.back.dto.ProfileSaveDto;
import ru.andrewb.charm.back.model.Gender;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Locale;

public class ProfileSaveRequestMapper {

    private static final ProfileSaveRequestMapper INSTANCE = new ProfileSaveRequestMapper();

    private ProfileSaveRequestMapper() {};

    public static ProfileSaveRequestMapper getInstance() {
        return INSTANCE;
    }

    public ProfileSaveDto map(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        ProfileSaveDto dto = new ProfileSaveDto();
        dto.setEmail(req.getParameter("email"));
        dto.setName(req.getParameter("name"));
        dto.setSurname(req.getParameter("surname"));
        dto.setAbout(req.getParameter("about"));

        String bd = req.getParameter("birthDate");
        if (bd == null || bd.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Param `birthDate` is required");
            return null;
        }
        try {
            dto.setBirthDate(LocalDate.parse(bd));
        } catch (DateTimeParseException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Param `birthDate` must be yyyy-MM-dd");
            return null;
        }

        String gp = req.getParameter("gender");
        if (gp == null || gp.isBlank()) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Param `gender` is required");
            return null;
        }
        try {
            dto.setGender(Gender.valueOf(gp.toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Param `gender` must be one of " + Arrays.toString(Gender.values()));
            return null;
        }

        return dto;
    }
}
