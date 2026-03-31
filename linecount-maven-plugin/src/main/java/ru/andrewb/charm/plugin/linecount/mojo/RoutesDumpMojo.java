package ru.andrewb.charm.plugin.linecount.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import ru.andrewb.charm.plugin.linecount.routes.JavaStringConstantsLoader;
import ru.andrewb.charm.plugin.linecount.routes.RoutesMarkdownRenderer;
import ru.andrewb.charm.plugin.linecount.routes.WebAnnotationRouteParser;
import ru.andrewb.charm.plugin.linecount.routes.model.RouteEntry;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static ru.andrewb.charm.plugin.linecount.utils.Utils.splitCsv;

@Mojo(
        name = "routes",
        defaultPhase = LifecyclePhase.PACKAGE,
        threadSafe = true
)
public class RoutesDumpMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}", readonly = true, required = true)
    private File baseDir;

    @Parameter(property = "routes.sourceDirs", defaultValue = "src/main/java")
    private String sourceDirsCsv;

    @Parameter(property = "routes.sourceDirs", defaultValue = "src/test/java")
    private String testDirCsv;

    @Parameter(property = "routes.includeTests", defaultValue = "false")
    private boolean includeTests;

    @Parameter(property = "routes.includeFilters", defaultValue = "false")
    private boolean includeFilters;

    @Parameter(property = "routes.output", defaultValue = "target/routes.md")
    private String output;

    @Parameter(property = "routes.charset", defaultValue = "${project.build.sourceEncoding}")
    private String charsetName;

    @Parameter(property = "routes.constantsIncludes", defaultValue = "**/Urls.java")
    private String constantsIncludes;

    @Parameter(property = "routes.constantsExcludes", defaultValue = "**/target/**,**/.git/**,**/.idea/**")
    private String constantsExcludes;

    JavaStringConstantsLoader loader = new JavaStringConstantsLoader();
    WebAnnotationRouteParser parser = new WebAnnotationRouteParser();
    RoutesMarkdownRenderer renderer = new RoutesMarkdownRenderer();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // Resolve charset
        Charset charset = (charsetName == null || charsetName.isBlank())
                ? Charset.defaultCharset()
                : Charset.forName(charsetName);

        // Load constants (NAME -> VALUE), e.g. PROFILE_URL -> "/profile"
        Path basePath = baseDir.toPath();
        Map<String, String> constants = loader.load(basePath, charset, constantsIncludes, constantsExcludes);
        getLog().info("Routes: loaded constants=" + constants.size());

        // Build list of roots to scan for routes
        List<Path> scanRoots = new ArrayList<>();
        for (String dir : splitCsv(sourceDirsCsv)) {
            scanRoots.add(basePath.resolve(dir));
        }
        if (includeTests) {
            scanRoots.add(basePath.resolve(testDirCsv));
        }

        // Scan sources and collect routes
        List<RouteEntry> routes = new ArrayList<>();
        for (Path root : scanRoots) {
            if (!Files.exists(root)) continue;

            // Walk through found files
            try (Stream<Path> walk = Files.walk(root)) {
                // Only search routes in regular .java files and put them into list
                walk.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".java"))
                        .forEach(p -> routes.addAll(parser.parseFile(p, charset, constants, includeFilters)));
            } catch (IOException e) {
                throw new MojoExecutionException("Failed to walk sources: " + root, e);
            }
        }

        // Sort found routes to make stable output
        routes.sort(Comparator
                .comparing(RouteEntry::type)
                .thenComparing(RouteEntry::clazz)
                .thenComparing(RouteEntry::patterns)
        );

        // Write markdown report
        Path outFile = basePath.resolve(output);
        try {
            if (outFile.getParent() != null) {
                Files.createDirectories(outFile.getParent());
            }
            Files.writeString(outFile, renderer.render(basePath, routes), charset);
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to write report: " + outFile, e);
        }

        getLog().info("Routes: found=" + routes.size() + ", output=" + outFile);
    }
}
