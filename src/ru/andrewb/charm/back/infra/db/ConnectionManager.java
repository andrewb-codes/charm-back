package ru.andrewb.charm.back.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.andrewb.charm.back.dto.Query;
import ru.andrewb.charm.back.utils.CustomDataSource;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Slf4j
@UtilityClass
public class ConnectionManager {

    private static volatile DataSource dataSource;
    private static volatile boolean initialized = false;

    private static volatile int fetchSize;
    private static volatile int maxRows;
    private static volatile int queryTimeout;

    public static synchronized void initOrThrow() {
        if (initialized) return;

        String url = Config.required("app.datasource.url");
        String user = Config.required("app.datasource.username");
        String password = Config.required("app.datasource.password");
        String driver = Config.getOrDefault("app.datasource.driver-class-name", "org.postgresql.Driver");
        int poolSize = Integer.parseInt(Config.getOrDefault("app.datasource.pool.size", "10"));

        fetchSize = Integer.parseInt(Config.getOrDefault("app.datasource.fetch-size", "100"));
        maxRows = Integer.parseInt(Config.getOrDefault("app.datasource.max-rows", "1000"));
        queryTimeout = Integer.parseInt(Config.getOrDefault("app.datasource.query-timeout", "10"));

        try {
            if (driver != null) Class.forName(driver);

            if (Config.getFF("use-custom-pool")) {
                dataSource = new CustomDataSource(url, user, password, poolSize);
            } else {
                var config = new HikariConfig();
                config.setJdbcUrl(url);
                config.setUsername(user);
                config.setPassword(password);
                config.setMaximumPoolSize(poolSize);
                config.setMinimumIdle(2);
                config.setConnectionTimeout(3_000);
                config.setIdleTimeout(60_000);
                config.setMaxLifetime(1_800_000);

                dataSource = new HikariDataSource(config);
            }

            initialized = true;

            log.info("DB pool initialized: maxPoolSize={}", poolSize);
            log.info("DB stmt defaults: fetchSize={}, maxRows={}, queryTimeout={}", fetchSize, maxRows, queryTimeout);

        } catch (Exception e) {
            log.error("Failed to init DB pool", e);
            throw new IllegalStateException("Failed to init DB pool", e);
        }
    }

    public static void ensureInit() {
        if (!initialized || dataSource == null) {
            throw new IllegalStateException("ConnectionManager is not initialized. Call initOrThrow() at startup.");
        }
    }

    public static Connection getConnection() throws SQLException {
        ensureInit();
        return dataSource.getConnection();
    }

    public static PreparedStatement getPreparedStmt(Connection conn, Query query) throws SQLException {
        ensureInit();

        PreparedStatement ps = conn.prepareStatement(query.sql());
        ps.setFetchSize(fetchSize);
        ps.setMaxRows(maxRows);
        ps.setQueryTimeout(queryTimeout);

        var args = query.args();
        for (int i = 0; i < args.size(); i++) {
            ps.setObject(i + 1, args.get(i));
        }
        return ps;
    }

    public static void closePool() throws IOException {
        var ds = dataSource;
        dataSource = null;
        initialized = false;
        if (ds != null) {
            ((Closeable) ds).close();
            log.info("DB pool closed");
        }
    }

    public void pingOrThrow() {
        ensureInit();
        try (Connection conn = ConnectionManager.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT 1");
             ResultSet rs = ps.executeQuery()) {

            if (!rs.next()) {
                log.error("DB ping failed: no rows returned");
                throw new IllegalStateException("DB ping failed: no rows returned");
            }

            int one = rs.getInt(1);
            if (one != 1) {
                log.error("DB ping failed: unexpected value={}", one);
                throw new IllegalStateException("DB ping failed: unexpected value=" + one);
            }

            log.info("DB ping OK");

        } catch (SQLException e) {
            log.error("DB is not available", e);
            throw new RuntimeException("DB is not available", e);
        }
    }
}
