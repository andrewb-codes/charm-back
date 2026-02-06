package ru.andrewb.charm.back.security;

import lombok.experimental.UtilityClass;
import org.mindrot.jbcrypt.BCrypt;

import static ru.andrewb.charm.back.validator.PasswordUtils.hasText;

@UtilityClass
public class PasswordHasher {
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
