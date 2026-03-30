package ru.andrewb.charm.plugin.linecount.mojo;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "help", threadSafe = true)
public class HelpMojo extends AbstractMojo {

    @Parameter(property = "linecount.help.detail", defaultValue = "false")
    private boolean detail;

    @Override
    public void execute() {
        getLog().info("");
        getLog().info("linecount-maven-plugin goals:");
        getLog().info("  - linecount:count    Count lines in project files");
        getLog().info("  - linecount:routes   Dump servlet/filter (@WebServlet/@WebFiltep) routes to target/routes.md");
        getLog().info("  - linecount:help     Show this help");
        getLog().info("");

        getLog().info("Usage examples:");
        getLog().info("  mvn linecount:count");
        getLog().info("  mvn linecount:count \"-Dlinecount.showFiles=true\"");
        getLog().info("  mvn -pl back linecount:routes");
        getLog().info("");

        if (!detail) {
            getLog().info("Run with -Dlinecount.help.detail=true for parameters list.");
            return;
        }

        // --- count ---
        getLog().info("Parameters for 'count':");
        getLog().info("  -Dlinecount.includes     Comma-separated glob patterns to include");
        getLog().info("       default: **/*.java,**/*.jsp,**/*.xml,**/*.properties");
        getLog().info("  -Dlinecount.excludes     Comma-separated glob patterns to exclude");
        getLog().info("       default: **/target/**,**/.git/**,**/.idea/**");
        getLog().info("  -Dlinecount.charset      Charset for reading files");
        getLog().info("       default: ${project.build.sourceEncoding} or platform default");
        getLog().info("  -Dlinecount.showFiles    Print per-file line counts");
        getLog().info("       default: false");
        getLog().info("  -Dlinecount.failOnMax    Fail build if total lines > N (-1 disables)");
        getLog().info("       default: -1");
        getLog().info("");

        // --- routes ---
        getLog().info("Parameters for 'routes':");
        getLog().info("  -Droutes.sourceDirs      Comma-separated source roots to scan (relative to module)");
        getLog().info("       default: src/main/java");
        getLog().info("  -Droutes.includeTests    Also scan src/test/java");
        getLog().info("       default: false");
        getLog().info("  -Droutes.includeFilters  Also scan @WebFilter");
        getLog().info("       default: false");
        getLog().info("  -Droutes.output          Output report path (relative to module)");
        getLog().info("       default: target/routes.md");
        getLog().info("  -Droutes.charset         Charset for reading sources");
        getLog().info("       default: ${project.build.sourceEncoding} or platform default");
        getLog().info("  -Droutes.constantsIncludes  Glob patterns for files with URL constants");
        getLog().info("       default: **/Urls.java");
        getLog().info("  -Droutes.constantsExcludes  Glob patterns to exclude during constants scan");
        getLog().info("       default: **/target/**,**/.git/**,**/.idea/**");
        getLog().info("");
    }
}
