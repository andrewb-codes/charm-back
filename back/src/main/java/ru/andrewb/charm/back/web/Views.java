package ru.andrewb.charm.back.web;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Views {
    private static final String JSP_DIR = "/WEB-INF/jsp";
    private static final String EXT = ".jsp";

    public static final String INDEX = "index";
    public static final String LOGIN = "login";
    public static final String REGISTRATION = "registration";
    public static final String PROFILE = "profile";
    public static final String PROFILES = "profiles";
    public static final String SETTINGS = "settings";
    public static final String CHARM = "charm";
    public static final String CHARM_EMPTY = "charm-empty";
    public static final String MATCHES = "matches";

    public static final String ERROR_400 = "error/400";
    public static final String ERROR_403 = "error/403";
    public static final String ERROR_404 = "error/404";
    public static final String ERROR_409 = "error/409";
    public static final String ERROR_500 = "error/500";

    public static String getJspPath(String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("url is required");
        }

        String clean = url.trim();

        if (clean.contains("..")) {
            throw new IllegalArgumentException("invalid url: " + url);
        }

        if ("/".equals(clean)) {
            return JSP_DIR + "/index" + EXT;
        }

        if (!clean.startsWith("/")) clean = "/" + clean;
        if (clean.endsWith(EXT)) clean = clean.substring(0, clean.length() - EXT.length());

        return JSP_DIR + clean + EXT;
    }
}
