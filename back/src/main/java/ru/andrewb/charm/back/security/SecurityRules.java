package ru.andrewb.charm.back.security;

import lombok.experimental.UtilityClass;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@UtilityClass
public class SecurityRules {

    public static boolean isSafeInternalRedirect(String contextPath, String back, String... allowedPrefixes) {
        if (back == null || back.isBlank()) return false;

        // decode in case %0d%0a etc.
        String decoded = URLDecoder.decode(back, StandardCharsets.UTF_8);
        if (decoded.contains("\r") || decoded.contains("\n")) return false;

        // have to be relative path without schema/host
        URI uri;
        try {
            uri = new URI(decoded);
        } catch (URISyntaxException e) {
            return false;
        }
        if (uri.isAbsolute()) return false;

        // normalize path, prohibit attempts to "exit" the context
        String path = uri.getPath() == null ? "" : uri.getPath();
        if (!path.startsWith(contextPath.endsWith("/") ? contextPath : contextPath + "/")
                && !path.equals(contextPath)) {
            return false;
        }
        // prohibit segments like '..'
        if (path.contains("/../") || path.endsWith("/..")) return false;

        // allow only specified prefixes
        for (String prefix : allowedPrefixes) {
            String full = (contextPath + prefix);
            if (path.startsWith(full)) return true;
        }
        return false;
    }
}
