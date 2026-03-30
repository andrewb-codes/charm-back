package ru.andrewb.charm.plugin.linecount.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static ru.andrewb.charm.plugin.linecount.utils.Utils.compileGlobs;
import static ru.andrewb.charm.plugin.linecount.utils.Utils.matchesAny;

@Mojo(
        name = "count",
        defaultPhase = LifecyclePhase.VERIFY,
        threadSafe = true
)
public class CountMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.basedir}", readonly = true, required = true)
    private File baseDir;

    @Parameter(property = "linecount.includes", defaultValue = "**/*.java,**/*.jsp,**/*.xml,**/*.properties")
    private String includes;

    @Parameter(property = "linecount.excludes", defaultValue = "**/target/**,**/.git/**,**/.idea/**")
    private String excludes;

    @Parameter(property = "linecount.charset", defaultValue = "${project.build.sourceEncoding}")
    private String charsetName;

    @Parameter(property = "linecount.showFiles", defaultValue = "false")
    private boolean showFiles;

    @Parameter(property = "linecount.failOnMax", defaultValue = "-1")
    private long failOnMax;

    @Override
    public void execute() throws MojoExecutionException {
        // Resolve charset
        Charset charset = (charsetName == null || charsetName.isBlank())
                ? Charset.defaultCharset()
                : Charset.forName(charsetName);

        // Compile glob strings into Java PathMatches objects
        List<Pattern> includeMatchers = compileGlobs(includes);
        List<Pattern> excludeMatchers = compileGlobs(excludes);

        long files = 0;
        long lines = 0;

        Map<String, Long> byExt = new TreeMap<>();

        Path basePath = baseDir.toPath();
        try (Stream<Path> walk = Files.walk(basePath)) {
            Iterator<Path> it = walk.iterator();
            while (it.hasNext()) {
                Path p = it.next();
                // Only count regular files (skip directories, etc.)
                if (!Files.isRegularFile(p)) continue;

                // Compute relative path (for matching patterns)
                Path relPath = basePath.relativize(p);
                String relUnix = relPath.toString().replace('\\', '/');
                // Incluse/exclude filtering
                if (!matchesAny(includeMatchers, relUnix)) continue;
                if (matchesAny(excludeMatchers, relUnix)) continue;

                // Count lines
                long c = countLines(p, charset);
                files++;
                lines += c;
                // Add to per-extension stats
                String ext = extension(p.getFileName().toString());
                byExt.merge(ext, c, Long::sum);

                if (showFiles) {
                    getLog().info(relUnix + " -> " + c);
                }
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to walk project files", e);
        }

        // Report
        getLog().info("LineCount: files=" + files + ", lines=" + lines);
        if (!byExt.isEmpty()) {
            getLog().info("Breakdown by extension:");
            byExt.forEach((k, v) -> getLog().info("  " + k + ": " + v));
        }
        if (failOnMax >= 0 && lines > failOnMax) {
            throw new MojoExecutionException("LineCount exceeded: " + lines + " > " + failOnMax);
        }
        getLog().info("");
    }

    // ---------------------------
    // Helpers
    // ---------------------------

    // Count physical lines in a file (includes empty lines and comments)
    private static long countLines(Path file, Charset charset) throws IOException {
        try (BufferedReader br = Files.newBufferedReader(file, charset)) {
            long c = 0;
            while (br.readLine() != null) c++;
            return c;
        }
    }

    // Extract lowercase extension ("noext" if missing)
    private static String extension(String name) {
        int i = name.lastIndexOf('.');
        if (i < 0 || i == name.length() - 1) return "(noext)";
        return name.substring(i + 1).toLowerCase(Locale.ROOT);
    }
}
