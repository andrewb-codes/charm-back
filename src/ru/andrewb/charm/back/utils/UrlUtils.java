package ru.andrewb.charm.back.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UrlUtils {

    public static final String INDEX_URL = "/";
    public static final String PROFILE_URL = "/profile";
    public static final String SETTINGS_URL = "/settings";
    public static final String EMAIL_URL = "/email";
    public static final String PASSWORD_URL = "/password";
    public static final String LOGIN_URL = "/login";
    public static final String LOGOUT_URL = "/logout";
    public static final String REGISTRATION_URL = "/registration";
    public static final String LANG_URL = "/lang";
    public static final String CONTENT_URL = "/content";
    public static final String REST_URL = "/api/v1";
    public static final String LOGIN_REST_URL = REST_URL + LOGIN_URL;
    public static final String PROFILE_REST_URL = REST_URL + PROFILE_URL;

    public static String getJspPath(String url) {
        return "/WEB-INF/jsp" + url + ".jsp";
    }
}
