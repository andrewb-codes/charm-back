package ru.andrewb.charm.back.bootstrap;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.service.ContentService;
import ru.andrewb.charm.back.utils.Config;

import java.nio.file.Files;
import java.nio.file.Path;

@WebListener
public class SetupCheck implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 1) check required configuration keys
        assertRequired(
                "app.datasource.url",
                "app.datasource.username",
                "app.datasource.password",
                "app.content.base-path"
        );

        // 2) force singletons initialization
        ContentService.getInstance();
        ProfileDao dao = ProfileDao.getInstance();

        // 3) check if content base path exists (or create)
        String basePathStr = Config.required("app.content.base-path");
        Path basePath = Path.of(basePathStr).toAbsolutePath().normalize();
        try {
            Files.createDirectories(basePath);
        } catch (Exception e) {
            throw new IllegalStateException("Content base path is invalid or not writable: " + basePath, e);
        }

        // 4) ping DB
        dao.ping();

        sce.getServletContext().log(
                "StartupCheck OK. Active profile=" + Config.getOrDefault("app.profile.active", "<none>")
        );
    }

    private static void assertRequired(String... keys) {
        StringBuilder missing = new StringBuilder();
        for (String k : keys) {
            String v = Config.get(k);
            if (v == null || v.isBlank()) {
                if (!missing.isEmpty()) missing.append(", ");
                missing.append(k);
            }
        }
        if (!missing.isEmpty()) {
            throw new IllegalStateException("Missing required config: " + missing);
        }
    }
}
