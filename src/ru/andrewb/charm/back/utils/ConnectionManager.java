package ru.andrewb.charm.back.utils;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.experimental.UtilityClass;
import ru.andrewb.charm.back.dto.Query;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@UtilityClass
public class ConnectionManager {

    private static final String URL = Config.required("app.datasource.url");
    private static final String USER = Config.required("app.datasource.username");
    private static final String PASSWORD = Config.required("app.datasource.password");
    private static final String DRIVER = Config.getOrDefault("app.datasource.driver-class-name", "org.postgresql.Driver");

    public static final int FETCH_SIZE = Integer.parseInt(
            Config.getOrDefault("app.datasource.fetch-size", "100"));
    public static final int MAX_ROWS = Integer.parseInt(
            Config.getOrDefault("app.datasource.max-rows", "1000"));
    public static final int QUERY_TIMEOUT = Integer.parseInt(
            Config.getOrDefault("app.datasource.query-timeout", "10"));
    public static final int POOL_SIZE = Integer.parseInt(
            Config.getOrDefault("app.datasource.pool.size", "10"));
    public static final long ACQUIRE_TIMEOUT_MS = Long.parseLong(
            Config.getOrDefault("app.datasource.pool.acquire-timeout-ms", "3000"));

    private static volatile DataSource dataSource;

    static {
        init();
    }

    private static void init() {
        try {
            if (DRIVER != null) Class.forName(DRIVER);

            if (Config.getFF("use-custom-pool")) {
                dataSource = new CustomDataSource(URL, USER, PASSWORD, POOL_SIZE, ACQUIRE_TIMEOUT_MS);
            } else {
                var config = new HikariConfig();
                config.setJdbcUrl(URL);
                config.setUsername(USER);
                config.setPassword(PASSWORD);
                config.setMaximumPoolSize(POOL_SIZE);
                config.setMinimumIdle(5);
                config.setConnectionTimeout(10000);
                config.setIdleTimeout(60000);
                config.setMaxLifetime(1800000);

                dataSource = new HikariDataSource(config);
            }

        } catch (Exception e) {
            throw new IllegalStateException("Failed to init ConnectionManager", e);
        }
    }

     public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }

    public static PreparedStatement getPreparedStmt(Connection conn, Query query) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(query.sql());
        ps.setFetchSize(FETCH_SIZE);
        ps.setMaxRows(MAX_ROWS);
        ps.setQueryTimeout(QUERY_TIMEOUT);

        var args = query.args();
        for (int i = 0; i < args.size(); i++) {
            ps.setObject(i + 1, args.get(i));
        }
        return ps;
    }

    public static void closePool() throws IOException {
        var ds = dataSource;
        if (ds != null) ((Closeable) ds).close();
    }
}
