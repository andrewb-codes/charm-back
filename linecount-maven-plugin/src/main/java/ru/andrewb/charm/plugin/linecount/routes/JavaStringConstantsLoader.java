package ru.andrewb.charm.plugin.linecount.routes;

import org.apache.maven.plugin.MojoExecutionException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static ru.andrewb.charm.plugin.linecount.utils.Utils.compileGlobs;
import static ru.andrewb.charm.plugin.linecount.utils.Utils.matchesAny;

public class JavaStringConstantsLoader {

    // Pattern: public static final String NAME = "value";
    private static final Pattern STRING_CONST = Pattern.compile(
            "\\bpublic\\s+static\\s+final\\s+String\\s+(\\w+)\\s*=\\s*\"([^\"]*)\"\\s*;",
            Pattern.MULTILINE
    );

    /**
     * Scans module sources for String constants.
     * Returns Map<String, String>:
     *     key   -> "CONST_NAME"
     *     value -> "value"
     */
    public Map<String, String> load(Path path, Charset charset, String includes, String excludes) throws MojoExecutionException {
        // Compile glob strings into Java PathMatches objects
        List<Pattern> includeMatchers = compileGlobs(includes);
        List<Pattern> excludeMatchers = compileGlobs(excludes);

        Map<String, String> constants = new HashMap<>();

        // Search constants only in src/main/java by default
        Path src = path.resolve("src/main/java");
        if (!Files.exists(src)) return constants;

        // Walk through src files
        try (Stream<Path> walk = Files.walk(src)) {
            // Only search constants in regular .java files
            walk.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".java"))
                    .forEach(p -> {
                        // Compute relative path (for matching patterns)
                        Path relPath = path.relativize(p);
                        String relUnix = relPath.toString().replace('\\', '/');
                        // Include/exclude filtering
                        if (!matchesAny(includeMatchers, relUnix)) return;
                        if (matchesAny(excludeMatchers, relUnix)) return;
                        // Search for pattern (public static final String NAME = "value";)
                        try {
                            String text = Files.readString(p, charset);
                            Matcher m = STRING_CONST.matcher(text);
                            while (m.find()) {
                                String name = m.group(1);
                                String value = m.group(2);
                                constants.put(name, value);
                            }
                        } catch (IOException ignore) {
                            // Skip unreadable constants file
                        }
                    });
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to scan constants under " + src, e);
        }
        return constants;
    }
}
