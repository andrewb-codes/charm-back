package ru.andrewb.charm.plugin.linecount.routes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RouteExpressionEvaluator {

    /**
     * Extracts and evaluates URL patterns from annotation arguments.
     * Supported forms:
     *   @@WebServlet("/login") -> "/login"
     *   @@WebServlet(value="/login") -> "/login"
     *   @@WebServlet(urlPatern="/login") -> "/login"
     *   @@WebServlet(PROFILE_URL+"/*") -> "/profile/*"
     *   @@WebServlet(REST_PREFIX+LOGIN_URL) -> "/api/v1/login"
     */
    public String resolveAnnotationsArgs(String annotationArg, Map<String, String> constants) {
        String s = annotationArg.trim();
        // Remove simple named attributes (leave only the value)
        s = s.replaceAll("\\b(value|urlPatterns)\\s*=\\s*", "");

        // Array case: { ... }
        if (s.startsWith("{") && s.endsWith("}")) {
            String inside = s.substring(1, s.length() - 1).trim();

            List<String> parts = splitTopLevelCsv(inside);

            List<String> out = new ArrayList<>();
            for (String part : parts) {
                // Evaluate string concatenation in routes
                String v = evalConcat(part.trim(), constants);
                if (!v.isBlank()) out.add(v);
            }
            return String.join(", ", out);
        }

        // Single expression case
        return evalConcat(s, constants);
    }

    /**
     * Splits a top-level comma-separated list by ',' but ignores comma inside string literals.
     * Example:
     *     { "/a", PROFILE_URL + "/a,b" } -> ["/a", "PROFILE_URL + "/a,b""]
     */
    private static List<String> splitTopLevelCsv(String s) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inString = false;

        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);

            if (ch == '"' && (i == 0 || s.charAt(i - 1) != '\\')) {
                inString = !inString;
                cur.append(ch);
                continue;
            }
            // Split on ',' only when not inside a string literal
            if (!inString && ch == ',') {
                out.add(cur.toString());
                cur.setLength(0);
                continue;
            }
            cur.append(ch);
        }
        out.add(cur.toString());
        return out;
    }

    /**
     * Evaluates string concatenation in routes.
     * Examples:
     *   PROFILE_URL + "/*"      -> "/profile/*"
     *   REST_PREFIX + LOGIN_URL -> "/api/v1/login"
     */
    private static String evalConcat(String expression, Map<String, String> constants) {
        List<String> tokens = splitByPlus(expression);

        StringBuilder sb = new StringBuilder();
        for (String t : tokens) {
            String part = resolveToken(t.trim(), constants);
            if (part == null) {
                return "";
            }
            sb.append(part);
        }
        return sb.toString();
    }

    /**
     * Splits a string expression by '+' but does not split inside "...".
     * Examples:
     *   PROFILE_URL + "/a+b"    -> ["PROFILE_URL", "/a+b"]
     *   REST_PREFIX + LOGIN_URL -> ["REST_PREFIX ", "LOGIN_URL"]
     */
    private static List<String> splitByPlus(String expression) {
        List<String> out = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inString = false;

        for (int i = 0; i < expression.length(); i++) {
            char ch = expression.charAt(i);

            if (ch == '"' && (i == 0 || expression.charAt(i - 1) != '\\')) {
                inString = !inString;
                cur.append(ch);
                continue;
            }
            // Split on '+' only when not inside a string literal
            if (!inString && ch == '+') {
                out.add(cur.toString());
                cur.setLength(0);
                continue;
            }
            cur.append(ch);
        }
        out.add(cur.toString());
        return out;
    }

    /**
     * Resolves one token (place between '+' signs in expression).
     * Examples:
     *   "/profile"  -> "/profile"
     *   PROFILE_URL -> "/profile"\
     */
    private static String resolveToken(String token, Map<String, String> constants) {
        if (token.isEmpty()) return "";

        // String literal token ("literal")
        if (token.startsWith("\"") && token.endsWith("\"") && token.length() >=2) {
            return token.substring(1, token.length() - 1);
        }
        // Constant token (CONSTANT_NAME)
        if (token.matches("[A-Z0-9_]+")) {
            return constants.get(token);
        }

        return null;
    }
}
