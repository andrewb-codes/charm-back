package ru.andrewb.charm.back.service;

import jakarta.servlet.ServletOutputStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.andrewb.charm.back.config.AppContentProperties;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.InfrastructureException;
import ru.andrewb.charm.back.model.exception.NotFoundException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.TRUNCATE_EXISTING;

@Slf4j
@Service
public class ContentService {

    private final Path basePath;

    public ContentService(AppContentProperties properties) {
        this.basePath = Path.of(properties.getBasePath()).toAbsolutePath().normalize();
        log.info("Content storage initialized basePath={}", this.basePath);
    }

    public void upload(InputStream inputStream, String contentPath) {
        try {
            Path full = getAbsolutePath(contentPath);
            Files.createDirectories(full.getParent());
            try (InputStream in = inputStream;
                 OutputStream out = Files.newOutputStream(full, CREATE, TRUNCATE_EXISTING)) {
                in.transferTo(out);
            }
            log.debug("Content uploaded path={}", full);
        } catch (IllegalArgumentException | SecurityException e) {
            log.warn("Content upload rejected path={}", contentPath);
            throw new BadRequestException("error.content.invalid-path");
        } catch (IOException e) {
            log.error("Content upload failed path={}", contentPath, e);
            throw new InfrastructureException("error.internal", e);
        }
    }

    public void upload(InputStream in, String... segments) {
        upload(in, String.join("/", segments));
    }

    public void download(ServletOutputStream out, String contentPath) {
        try {
            Path full = getAbsolutePath(contentPath);
            if (!Files.exists(full)) {
                log.warn("Content not found path={}", full);
                throw new NotFoundException("error.content.not-found");
            }
            try (InputStream in = Files.newInputStream(full)) {
                in.transferTo(out);
                out.flush();
            }
            log.debug("Content downloaded path={}", full);
        } catch (IllegalArgumentException | SecurityException e) {
            log.warn("Content download rejected path={}", contentPath);
            throw new BadRequestException("error.content.invalid-path");
        } catch (IOException e) {
            log.error("Content download failed path={}", contentPath, e);
            throw new InfrastructureException("error.internal", e);
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
            Path path = resolve(contentPath);
            boolean deleted = Files.deleteIfExists(path);
            log.debug("Content delete path={} deleted={}", path, deleted);
        } catch (IOException e) {
            log.error("Content delete failed path={}", contentPath, e);
            throw new InfrastructureException("error.internal", e);
        }
    }

    public void delete(String... segments) {
        try {
            Path path = resolve(segments);
            boolean deleted = Files.deleteIfExists(path);
            log.debug("Content delete path={} deleted={}", path, deleted);
        } catch (IOException e) {
            log.error("Content delete failed path={}", String.join("/", segments), e);
            throw new InfrastructureException("error.internal", e);
        }
    }

    public void deleteTree(String... segments) {
        Path dir = resolve(segments);
        if (!Files.exists(dir)) {
            log.debug("Content delete tree skipped path={} reason=not-found", dir);
            return;
        }
        try (var walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            throw new InfrastructureException("error.internal", e);
                        }
                    });
            log.debug("Content tree deleted path={}", dir);
        } catch (IOException e) {
            log.error("Content delete tree failed path={}", dir, e);
            throw new InfrastructureException("error.internal", e);
        }
    }

    private Path getAbsolutePath(String contentPath) {
        if (contentPath == null || contentPath.isBlank()) {
            throw new IllegalArgumentException("contentPath is required");
        }
        String clean = contentPath.startsWith("/") ? contentPath.substring(1) : contentPath;
        Path full = basePath.resolve(clean).normalize();
        if (!full.startsWith(basePath)) {
            throw new SecurityException("invalid content path");
        }
        return full;
    }
}
