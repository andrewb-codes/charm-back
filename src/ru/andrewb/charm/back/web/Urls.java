package ru.andrewb.charm.back.web;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Urls {
    public static final String INDEX_URL = "/";
    public static final String PROFILE_URL = "/profile";
    public static final String PROFILES_URL = "/profiles";
    public static final String SETTINGS_URL = "/settings";
    public static final String EMAIL_URL = "/email";
    public static final String PASSWORD_URL = "/password";
    public static final String LOGIN_URL = "/login";
    public static final String LOGOUT_URL = "/logout";
    public static final String REGISTRATION_URL = "/registration";
    public static final String LANG_URL = "/lang";
    public static final String CONTENT_URL = "/content";
    public static final String CHARM_URL = "/charm";
    public static final String CHARM_EMPTY_URL = "/charm-empty";
    public static final String MATCHES_URL = "/matches";

    public static final String REST_PREFIX = "/api/v1";
    public static final String LOGIN_REST_URL = REST_PREFIX + LOGIN_URL;
    public static final String LOGOUT_REST_URL = REST_PREFIX + LOGOUT_URL;
    public static final String REGISTRATION_REST_URL = REST_PREFIX + REGISTRATION_URL;
    public static final String PROFILE_REST_URL = REST_PREFIX + PROFILE_URL;
    public static final String PROFILES_REST_URL = REST_PREFIX + PROFILES_URL;
    public static final String CHARM_REST_URL = REST_PREFIX + CHARM_URL;
    public static final String MATCHES_REST_URL = REST_PREFIX + MATCHES_URL;
}
