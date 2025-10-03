package ru.andrewb.charm.back.utils;

import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import ru.andrewb.charm.back.model.exception.BadRequestException;

@UtilityClass
public final class RequestParams {
    public static long requirePositiveLong(HttpServletRequest req, String name) {
        String raw = req.getParameter(name);
        if (raw == null || raw.isBlank()) {
            throw new BadRequestException("Param '" + name + "' is required");
        }
        try {
            long v = Long.parseLong(raw);
            if (v <= 0) throw new NumberFormatException("non-positive");
            return v;
        } catch (NumberFormatException e) {
            throw new BadRequestException("Param '" + name + "' must be positive long");
        }
    }
}