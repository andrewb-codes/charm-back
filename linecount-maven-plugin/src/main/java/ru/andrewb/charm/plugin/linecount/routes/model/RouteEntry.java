package ru.andrewb.charm.plugin.linecount.routes.model;

import java.nio.file.Path;

public record RouteEntry(
        String type,     // "servlet" or "filter"
        String clazz,    // fully qualified file name
        String patterns, // resolved patterns
        Path source      // source file path
) { }
