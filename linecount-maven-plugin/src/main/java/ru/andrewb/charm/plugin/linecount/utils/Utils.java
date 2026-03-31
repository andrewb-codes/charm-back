package ru.andrewb.charm.plugin.linecount.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class Utils {

    // Converts comma-separated globs into matchers ("**/*.java,**/*.jsp" -> [PathMatcher...])
    public static List<Pattern> compileGlobs(String csvGlobs) {
        if (csvGlobs == null || csvGlobs.isBlank()) return List.of();
        List<String> globs = splitCsv(csvGlobs);
        List<Pattern> out = new ArrayList<>();
        for (String glob : globs) {
            out.add(Pattern.compile(globToRegex(glob)));
        }
        return out;
    }

    // Returns true if relUnix (path string with '/' separators) matches any compiled glob patterns
    public static boolean matchesAny(List<Pattern> globs, String relUnix) {
        if (globs.isEmpty()) return false;
        for (Pattern p : globs) {
            if (p.matcher(relUnix).matches()) return true;
        }
        return false;
    }

    // Splits string by "," and returns list of not empty parts
    public static List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        String[] parts = csv.split(",");
        List<String> out = new ArrayList<>();
        for (String s : parts) {
            String t = s.trim();
            if (!t.isEmpty()) out.add(t);
        }
        return out;
    }

    /**
     * Minimal glob -> regex conversion supporting:
     *   **  : any chars including '/'
     *   *   : any chars except '/'
     *   ?   : exactly one char except '/'
     */
    private static String globToRegex(String glob) {
        glob = glob.replace('\\', '/');

        StringBuilder sb = new StringBuilder();
        sb.append("^");

        // If glob starts with "**/" -> allow "target/..." and "a/b/target/..."
        if (glob.startsWith("**/")) {
            sb.append("(?:.*/)?");
            glob = glob.substring(3); // remove leading "**/"
        }

        for (int i = 0; i < glob.length(); i++) {
            char c = glob.charAt(i);

            if (c == '*') {
                // check for '**'
                boolean isDouble = (i + 1 < glob.length() && glob.charAt(i + 1) == '*');
                if (isDouble) {
                    sb.append(".*");
                    i++; // consume second '**'
                } else {
                    sb.append("[^/]*");
                }
                continue;
            }
            if (c == '?') {
                sb.append("[^/]");
                continue;
            }
            // escape regex special char
            if ("\\.[]{}()+-^$|".indexOf(c) >= 0) sb.append("\\");
            sb.append(c);
        }

        sb.append("$");
        return sb.toString();
    }
}
