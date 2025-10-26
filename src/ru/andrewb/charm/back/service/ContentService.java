package ru.andrewb.charm.back.service;

import jakarta.servlet.ServletOutputStream;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.NotFoundException;
import ru.andrewb.charm.back.utils.Config;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ContentService {

    private static final ContentService INSTANCE = new ContentService();

    private static final Path BASE_PATH = Path.of(
            Config.required("app.content.base-path")
    ).toAbsolutePath().normalize();

    public static ContentService getInstance() {
        return INSTANCE;
    }

    public void upload(InputStream inputStream, String contentPath) {
        try {
            Path full = getAbsolutePath(contentPath);
            Files.createDirectories(full.getParent());
            try (InputStream in = inputStream;
                 OutputStream out = Files.newOutputStream(full, CREATE, TRUNCATE_EXISTING)) {
                in.transferTo(out);
            }
        } catch (IllegalArgumentException | SecurityException e) {
            throw new BadRequestException("error.content.invalid-path");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void upload(InputStream in, String... segments) {
        upload(in, String.join("/", segments));
    }

    public void download(ServletOutputStream out, String contentPath) {
        try {
            Path full = getAbsolutePath(contentPath);
            if (!Files.exists(full)) {
                throw new NotFoundException("error.content.not-found");
            }
            try (InputStream in = Files.newInputStream(full)) {
                in.transferTo(out);
                out.flush();
            }
        } catch (IllegalArgumentException | SecurityException e) {
            throw new BadRequestException("error.content.invalid-path");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void download(ServletOutputStream out, String... segments) {
        download(out, String.join("/", segments));
    }


    public Path resolve(String contentPath) {
        try {
            return getAbsolutePath(contentPath);
        } catch (IllegalArgumentException | SecurityException e) {
            throw new BadRequestException("error.content.invalid-path");
        }
    }

    public Path resolve(String... segments) {
        try {
            if (segments == null || segments.length == 0) {
                throw new IllegalArgumentException("segments are required");
            }
            return getAbsolutePath(String.join("/", segments));
        } catch (IllegalArgumentException | SecurityException e) {
            throw new BadRequestException("error.content.invalid-path");
        }
    }

    public void delete(String contentPath) {
        try {
            Files.deleteIfExists(resolve(contentPath));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void delete(String... segments) {
        try {
            Files.deleteIfExists(resolve(segments));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteTree(String... segments) {
        Path dir = resolve(segments);
        if (!Files.exists(dir)) return;
        try (var walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Path getAbsolutePath(String contentPath) {
        if (contentPath == null || contentPath.isBlank()) {
            throw new IllegalArgumentException("contentPath is required");
        }
        String clean = contentPath.startsWith("/") ? contentPath.substring(1) : contentPath;
        Path full = BASE_PATH.resolve(clean).normalize();
        if (!full.startsWith(BASE_PATH)) {
            throw new SecurityException("invalid content path");
        }
        return full;
    }
}