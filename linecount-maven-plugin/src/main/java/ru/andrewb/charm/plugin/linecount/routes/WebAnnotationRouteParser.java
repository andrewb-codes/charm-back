package ru.andrewb.charm.plugin.linecount.routes;

import ru.andrewb.charm.plugin.linecount.routes.model.RouteEntry;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebAnnotationRouteParser {

    // Pattern: package ru.foo.bar
    private static final Pattern PACKAGE = Pattern.compile("^\\s*package\\s+([\\w\\.]+)\\s*;", Pattern.MULTILINE);
    // Pattern: class MyController
    private static final Pattern CLASS = Pattern.compile("\\bclass\\s+(\\w+)\\b");
    // Pattern: @WebServlet(...)
    private static final Pattern WEBSERVLET = Pattern.compile("@WebServlet\\s*\\(([^)]*)\\)", Pattern.DOTALL);
    // Pattern: WebFilter(...)
    private static final Pattern WEBFILTER = Pattern.compile("@WebFilter\\s*\\(([^)]*)\\)", Pattern.DOTALL);

    private final RouteExpressionEvaluator evaluator = new RouteExpressionEvaluator();

    /**
     * Parses Java file, find and resolve routes.
     * Returns List<RouteEntry> or empty list if file can't be read.
     * Each of RouteEntry object contains information about found route:
     *     String type     -> "servlet" or "filter"
     *     String clazz    -> fully qualified file name
     *     String patterns -> resolved patterns
     *     Path source     -> source file path
     */
    public List<RouteEntry> parseFile(Path javaFile, Charset charset, Map<String, String> constants, boolean includeFilters) {

        String text;
        try {
            text = Files.readString(javaFile, charset);
        } catch (IOException e) {
            return List.of();
        }

        // Get FQCN (package + class name)
        String pkg = findGroup(PACKAGE, text).orElse("(default)");
        String cls = findGroup(CLASS, text).orElse(stripExt(javaFile.getFileName().toString()));
        String fqcn = "(default)".equals(pkg) ? cls : pkg + "." + cls;

        List<RouteEntry> routes = new ArrayList<>();
        // WebServlet routes
        Matcher ms = WEBSERVLET.matcher(text);
        while (ms.find()) {
            String args = ms.group(1);
            // Extract and resolve found annotation arguments
            String patterns = evaluator.resolveAnnotationsArgs(args, constants);
            if (!patterns.isBlank()) {
                routes.add(new RouteEntry("servlet", fqcn, patterns, javaFile));
            }
        }
        // WebFilter routes (optional)
        if (includeFilters) {
            Matcher mf = WEBFILTER.matcher(text);
            while (mf.find()) {
                String args = mf.group(1);
                // Extract and resolve found annotation arguments
                String patterns = evaluator.resolveAnnotationsArgs(args, constants);
                if (!patterns.isBlank()) {
                    routes.add(new RouteEntry("filter", fqcn, patterns, javaFile));
                }
            }
        }
        return routes;
    }

    // ---------------------------
    // Helpers
    // ---------------------------

    // Finds the first match of a regex in text and returns capture group #1 (or empty if not found)
    public static Optional<String> findGroup(Pattern pattern, String text) {
        Matcher m = pattern.matcher(text);
        if (m.find()) return Optional.ofNullable(m.group(1));
        return Optional.empty();
    }

    // Returns the filename without its last extension (e.g., "A.java -> "A"); unchanged if no dot.
    public static String stripExt(String fileName) {
        int i = fileName.lastIndexOf(".");
        return (i <= 0) ? fileName : fileName.substring(0, i);
    }
}
