package ru.andrewb.charm.back.utils;

import lombok.experimental.UtilityClass;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@UtilityClass
public class Config {

    private static final Properties FILE_PROPS = new Properties();
    private static volatile boolean initialized = false;

    public static synchronized void init() {
        if (initialized) return;
        // 1) base
        loadIfPresent("application.properties");
        // 2) profile
        String profile = firstNotBlank(
                System.getProperty("app.profile.active"),
                System.getenv("APP_PROFILE")
        );
        if (profile != null && !profile.isBlank()) {
            loadIfPresent("application-" + profile + ".properties");
        }

        initialized = true;
    }

    private static void ensureInit() {
        if (!initialized) {
            throw new IllegalStateException("Config is not initialized. Call Config.init() at startup.");
        }
    }

    public static String get(String key) {
        ensureInit();

        // 1) sysprops
        String v = System.getProperty(key);
        if (v != null) return v;

        // 2) env (SOME_KEY or SPRING_DATASOURCE_URL etc.)
        v = System.getenv(toEnvKey(key));
        if (v != null) return v;

        // 3) files
        return FILE_PROPS.getProperty(key);
    }

    public static boolean getFF(String ffKey) {
        return Boolean.parseBoolean(get("app.ff." + ffKey));
    }

    public static String getOrDefault(String key, String def) {
        String v = get(key);
        return (v != null) ? v : def;
    }

    public static String required(String key) {
        String v = get(key);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Missing required config: " + key);
        }
        return v;
    }

    // --- helpers ---
    private static void loadIfPresent(String name) {
        try (InputStream in = Config.class.getClassLoader().getResourceAsStream(name)) {
            if (in != null) {
                FILE_PROPS.load(in);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load " + name, e);
        }
    }

    private static String firstNotBlank(String... vals) {
        for (String v : vals) if (v != null && !v.isBlank()) return v;
        return null;
    }

    private static String toEnvKey(String key) {
        // app.datasource.url -> APP_DATASOURCE_URL
        return key.replace('.', '_').replace('-', '_').toUpperCase();
    }
}
