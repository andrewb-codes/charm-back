package ru.andrewb.charm.back.security;

import lombok.experimental.UtilityClass;
import org.mindrot.jbcrypt.BCrypt;

@UtilityClass
public class PasswordHasher {

    public static String hashPwd(String pwd) {
        return BCrypt.hashpw(pwd, BCrypt.gensalt());
    }

    public static boolean checkPwd(String pwd, String hash) {
        if (!hasPwd(pwd) || ! hasPwd(hash)) return false;
        try {
            return BCrypt.checkpw(pwd, hash);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static boolean hasPwd(String pwd) {
        return pwd != null && !pwd.isBlank();
    }
}
