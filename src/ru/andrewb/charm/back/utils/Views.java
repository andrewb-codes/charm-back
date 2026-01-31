package ru.andrewb.charm.back.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Views {
    private static final String JSP_DIR = "/WEB-INF/jsp";
    private static final String EXT = ".jsp";

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
