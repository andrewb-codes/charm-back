package ru.andrewb.charm.back.utils;

import lombok.experimental.UtilityClass;
import ru.andrewb.charm.back.dto.Query;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@UtilityClass
public class ConnectionManager {

    private static final String URL =
            Config.required("app.datasource.url");
    private static final String USER =
            Config.required("app.datasource.username");
    private static final String PASSWORD =
            Config.required("app.datasource.password");
    private static final String DRIVER =
            Config.getOrDefault("app.datasource.driver-class-name", "org.postgresql.Driver");
    private static final String FETCH_SIZE_STR =
            Config.get("app.datasource.fetch-size");
    public static final int FETCH_SIZE =
            Integer.parseInt(FETCH_SIZE_STR != null ? FETCH_SIZE_STR : "100");
    private static final String MAX_ROWS_STR =
            Config.get("app.datasource.max-rows");
    public static final int MAX_ROWS =
            Integer.parseInt(MAX_ROWS_STR != null ? MAX_ROWS_STR : "1000");
    private static final String QUERY_TIMEOUT_STR =
            Config.get("app.datasource.query-timeout");
    public static final int QUERY_TIMEOUT =
            Integer.parseInt(QUERY_TIMEOUT_STR != null ? QUERY_TIMEOUT_STR : "10");

    static {
        if (DRIVER != null) {
            try {
                Class.forName(DRIVER);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public static PreparedStatement getPreparedStmt(Connection conn, Query query) throws SQLException {
        PreparedStatement ps = conn.prepareStatement(query.sql());
        List<Object> args = query.args();
        for (int i = 0; i < args.size(); i++) {
            ps.setObject(i + 1, args.get(i));
        }
        return ps;
    }
}
