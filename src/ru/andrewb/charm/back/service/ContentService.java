package ru.andrewb.charm.back.service;

import jakarta.servlet.ServletOutputStream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ContentService {

    private static final ContentService INSTANCE = new ContentService();

    public static ContentService getInstance() {
        return INSTANCE;
    }

    public void upload(String contentPath, InputStream inputStream) throws IOException {
        Path full = getAbsolutePath(contentPath);
        Files.createDirectories(full.getParent());
        try (InputStream in = inputStream;
             OutputStream out = Files.newOutputStream(full, CREATE, TRUNCATE_EXISTING)) {
            in.transferTo(out);
        }
    }
    
    public void download(String contentPath, ServletOutputStream out) throws IOException {
        Path full = getAbsolutePath(contentPath);
        if (!Files.exists(full)) throw new FileNotFoundException();
        try (InputStream in = Files.newInputStream(full)) {
            in.transferTo(out);
            out.flush();
        }
    }

    private Path getAbsolutePath(String contentPath) {
        String basePath = "/Users/andrew/Downloads"; // TODO: move to config/ENV
        Path base = Path.of(basePath).toAbsolutePath().normalize();

        String clean = contentPath.startsWith("/") ? contentPath.substring(1) : contentPath;
        Path full = base.resolve(clean).normalize();

        if (!full.startsWith(base)) {
            throw new SecurityException("invalid content path");
        }
        return full;
    }
}