package ru.andrewb.charm.back.utils;

import lombok.experimental.UtilityClass;
import ru.andrewb.charm.back.model.exception.BadRequestException;

@UtilityClass
public class Passwords {

    public static String normalize(String raw) {
        return raw == null ? null : raw.trim();
    }

    public static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    public static boolean lengthAtLeast(String s, int minLen) {
        return s != null && s.length() >= minLen;
    }

    public static String requireValidOrThrow(String rawPassword, int minLen) {
        String pwd = Passwords.normalize(rawPassword);
        if (!Passwords.hasText(pwd)) {
            throw new BadRequestException("error.password.required");
        }
        if (!Passwords.lengthAtLeast(pwd, minLen)) {
            throw new BadRequestException("error.password.too-short");
        }
        return pwd;
    }
}
