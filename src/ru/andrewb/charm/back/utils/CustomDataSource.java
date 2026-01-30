package ru.andrewb.charm.back.utils;

import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;


@Slf4j
public class CustomDataSource implements DataSource, Closeable {

    private final String url;
    private final String user;
    private final String password;
    private final long acquireTimeoutMs;

    private final BlockingQueue<Connection> pool;
    private final List<Connection> physicalConnections = new ArrayList<>();
    private final AtomicBoolean closed = new AtomicBoolean(false);

    public CustomDataSource(String url, String user, String password, int poolSize, long acquireTimeoutMs) throws SQLException {
        this.url = url;
        this.user = user;
        this.password = password;
        this.acquireTimeoutMs = acquireTimeoutMs;

        this.pool = new ArrayBlockingQueue<>(poolSize, true);

        for (int i = 0; i < poolSize; i++) {
            Connection physical = DriverManager.getConnection(url, user, password);
            physicalConnections.add(physical);
            pool.add(new ProxyConnection(physical, pool));
        }
        log.info("CustomDataSource initialized: poolSize={}", poolSize);
    }

    @Override
    public Connection getConnection() throws SQLException {
        if (closed.get()) {
            throw new SQLException("DataSource is closed");
        }
        try {
            Connection conn = pool.poll(acquireTimeoutMs , TimeUnit.MILLISECONDS);
            if (conn == null) {
                throw new SQLException("No free DB connections (timeout " + acquireTimeoutMs + " ms)");
            }
            if (conn instanceof ProxyConnection pc) {
                pc.markBorrowed();
            }
            return conn;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for DB connection", e);
        }
    }

    @Override
    public void close() {
        if (!closed.compareAndSet(false, true)) return;

        for (Connection c : physicalConnections) {
            try {
                c.close();
            } catch (SQLException e) {
                log.warn("Error closing physical connection", e);
            }
        }
        pool.clear();
        log.info("CustomDataSource closed");
    }

    @Override
    public Connection getConnection(String user, String password) throws SQLException {
        if (!Objects.equals(user, this.user) || !Objects.equals(password, this.password)) {
            throw new SQLFeatureNotSupportedException("Different credentials are not supported");
        }
        return getConnection();
    }

    @Override
    public PrintWriter getLogWriter() {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out){
    }

    @Override
    public void setLoginTimeout(int seconds) {
    }

    @Override
    public int getLoginTimeout() {
        return 0;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        throw new SQLFeatureNotSupportedException();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        return false;
    }
}
