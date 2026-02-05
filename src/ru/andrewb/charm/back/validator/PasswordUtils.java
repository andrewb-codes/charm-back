package ru.andrewb.charm.back.utils;

import lombok.experimental.UtilityClass;
import org.mindrot.jbcrypt.BCrypt;
import ru.andrewb.charm.back.model.exception.BadRequestException;

@UtilityClass
public class PasswordUtils {

    public static String normalize(String pwd) {
        return pwd == null ? null : pwd.trim();
    }

    public static boolean hasText(String pwd) {
        return pwd != null && !pwd.isBlank();
    }

    public static boolean lengthAtLeast(String pwd, int minLen) {
        return pwd != null && pwd.length() >= minLen;
    }

    public static String requireValidOrThrow(String pwd, int minLen) {
        String pwdClean = PasswordUtils.normalize(pwd);
        if (!PasswordUtils.hasText(pwdClean)) {
            throw new BadRequestException("error.password.required");
        }
        if (!PasswordUtils.lengthAtLeast(pwdClean, minLen)) {
            throw new BadRequestException("error.password.short");
        }
        return pwdClean;
    }

    public static String hashPwd(String pwd) {
        return BCrypt.hashpw(pwd, BCrypt.gensalt());
    }

    public static boolean checkPwd(String pwd, String hash) {
        if (!hasText(pwd) || ! hasText(hash)) return false;
        try {
            return BCrypt.checkpw(pwd, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }

    }
}