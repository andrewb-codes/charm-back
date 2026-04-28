package ru.andrewb.charm.back.service;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ru.andrewb.charm.back.config.AppContentProperties;
import ru.andrewb.charm.back.model.exception.BadRequestException;
import ru.andrewb.charm.back.model.exception.NotFoundException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ContentServiceTest {

    @TempDir
    Path tempDir;

    private ContentService contentService;

    @BeforeEach
    void setUp() {
        AppContentProperties properties = new AppContentProperties();
        properties.setBasePath(tempDir.toString());
        contentService = new ContentService(properties);
    }

    @Test
    void uploadAndDownload_shouldPersistAndReturnContent() {
        byte[] payload = "hello-content".getBytes(StandardCharsets.UTF_8);

        contentService.upload(new ByteArrayInputStream(payload), "profile/1/photo.txt");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        contentService.download(new TestServletOutputStream(out), "profile/1/photo.txt");

        assertArrayEquals(payload, out.toByteArray());
    }

    @Test
    void upload_shouldThrowBadRequest_whenPathTraversalIsUsed() {
        byte[] payload = "blocked".getBytes(StandardCharsets.UTF_8);

        BadRequestException ex = assertThrows(
                BadRequestException.class,
                () -> contentService.upload(new ByteArrayInputStream(payload), "../outside.txt")
        );

        assertEquals("error.content.invalid-path", ex.getMessage());
    }

    @Test
    void download_shouldThrowNotFound_whenFileDoesNotExist() {
        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> contentService.download(new TestServletOutputStream(new ByteArrayOutputStream()), "profile/2/missing.jpg")
        );

        assertEquals("error.content.not-found", ex.getMessage());
    }

    @Test
    void deleteTree_shouldRemoveDirectoryRecursively() throws IOException {
        Path profileDir = tempDir.resolve("profile").resolve("3");
        Files.createDirectories(profileDir);
        Files.writeString(profileDir.resolve("a.txt"), "a");
        Files.createDirectories(profileDir.resolve("nested"));
        Files.writeString(profileDir.resolve("nested").resolve("b.txt"), "b");

        contentService.deleteTree("profile", "3");

        assertFalse(Files.exists(profileDir));
    }

    private static final class TestServletOutputStream extends ServletOutputStream {
        private final ByteArrayOutputStream out;

        private TestServletOutputStream(ByteArrayOutputStream out) {
            this.out = out;
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setWriteListener(WriteListener writeListener) {
            // not needed in unit tests
        }

        @Override
        public void write(int b) {
            out.write(b);
        }
    }
}

