package ru.andrewb.charm.back.security;

import lombok.experimental.UtilityClass;

import java.util.Set;

import static ru.andrewb.charm.back.utils.UrlUtils.*;

@UtilityClass
public class SecurityRules {

    public static final Set<String> PUBLIC_UI = Set.of(
            INDEX_URL, LOGIN_URL, REGISTRATION_URL, LOGOUT_URL, LANG_URL, CONTENT_URL
    );

    public static final Set<String> PUBLIC_REST = Set.of(
            LOGIN_REST_URL
    );

    public static boolean isRest(String path) {
        return path != null && path.startsWith(REST_URL);
    }
}
