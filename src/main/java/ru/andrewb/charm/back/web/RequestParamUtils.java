package ru.andrewb.charm.back.web;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import ru.andrewb.charm.back.model.exception.BadRequestException;

@UtilityClass
public final class RequestParamUtils {
    public static long requirePositiveLong(HttpServletRequest req, String name) {
        String raw = req.getParameter(name);
        if (raw == null || raw.isBlank()) {
            throw new BadRequestException("error.param.required");
        }
        try {
            long v = Long.parseLong(raw);
            if (v <= 0) throw new BadRequestException("error.param.invalid");
            return v;
        } catch (NumberFormatException e) {
            throw new BadRequestException("error.param.invalid");
        }
    }

    public static String rid(HttpServletRequest req) {
        Object v = req.getAttribute("rid");
        return v == null ? "-" : v.toString();
    }
}