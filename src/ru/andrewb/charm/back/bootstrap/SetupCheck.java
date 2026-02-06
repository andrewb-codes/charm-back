package ru.andrewb.charm.back.bootstrap;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import ru.andrewb.charm.back.dao.ProfileDao;
import ru.andrewb.charm.back.dao.ProfileLikeDao;
import ru.andrewb.charm.back.service.ContentService;
import ru.andrewb.charm.back.config.Config;
import ru.andrewb.charm.back.infra.db.ConnectionManager;
import ru.andrewb.charm.back.infra.cache.RedisManager;

import java.nio.file.Files;
import java.nio.file.Path;

@WebListener
public class SetupCheck implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        // 0) init config first
        Config.init();

        // 1) check required configuration keys
        assertRequired(
                "app.datasource.url",
                "app.datasource.username",
                "app.datasource.password",
                "app.content.base-path",
                "app.redis.host",
                "app.redis.port"
        );

        // 2) init pools/clients explicitly
        ConnectionManager.initOrThrow();
        RedisManager.initOrThrow();

        // 3) init singletons
        ContentService.getInstance();
        ProfileDao.getInstance();
        ProfileLikeDao.getInstance();

        // 4) check if content base path exists (or create)
        String basePathStr = Config.required("app.content.base-path");
        Path basePath = Path.of(basePathStr).toAbsolutePath().normalize();
        try {
            Files.createDirectories(basePath);
        } catch (Exception e) {
            throw new IllegalStateException("Content base path is invalid or not writable: " + basePath, e);
        }

        // 5) ping DB and Redis
        ConnectionManager.pingOrThrow();
        RedisManager.pingOrThrow();

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
