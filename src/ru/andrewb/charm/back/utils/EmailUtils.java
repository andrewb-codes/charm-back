package ru.andrewb.charm.back.utils;

import lombok.experimental.UtilityClass;
import ru.andrewb.charm.back.model.exception.BadRequestException;

import java.util.regex.Pattern;

@UtilityClass
public class EmailUtils {
    private static final Pattern EMAIL_RE = Pattern.compile(
            "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    public static String normalize(String raw) {
        return raw == null ? null : raw.trim();
    }

    public static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    public static boolean matchesFormat(String normalized) {
        return normalized != null && EMAIL_RE.matcher(normalized).matches();
    }

    public static String requireValidOrThrow(String rawEmail) {
        String email = EmailUtils.normalize(rawEmail);
        if (!EmailUtils.hasText(email)) {
            throw new BadRequestException("error.email.required");
        }
        if (!EmailUtils.matchesFormat(email)) {
            throw new BadRequestException("error.email.invalid");
        }
        return email;
    }
}
