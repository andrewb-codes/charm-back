package ru.andrewb.charm.back.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.filter.HiddenHttpMethodFilter;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import ru.andrewb.charm.pool.CustomDataSource;
import ru.andrewb.charm.pool.dto.CustomDataSourceConfig;

import javax.sql.DataSource;
import java.sql.SQLException;

@Configuration
public class InfraConfig {

    @Bean
    public DataSource dataSource(AppDataSourceProperties properties) {
        String driver = properties.getDriverClassName() != null
                ? properties.getDriverClassName()
                : "org.postgresql.Driver";

        String poolImpl = properties.getPoolImpl() != null
                ? properties.getPoolImpl()
                : "";

        int poolSize = properties.getPoolSize() != null
                ? properties.getPoolSize()
                : 10;

        try {
            Class.forName(driver);

            if ("hikari".equals(poolImpl)) {
                HikariConfig config = new HikariConfig();
                config.setJdbcUrl(properties.getUrl());
                config.setUsername(properties.getUsername());
                config.setPassword(properties.getPassword());
                config.setDriverClassName(driver);
                config.setMaximumPoolSize(poolSize);
                config.setMinimumIdle(2);
                config.setConnectionTimeout(10_000);
                config.setIdleTimeout(60_000);
                config.setMaxLifetime(1_800_000);

                return new HikariDataSource(config);
            }

            CustomDataSourceConfig config = new CustomDataSourceConfig();
            config.setJdbcUrl(properties.getUrl());
            config.setUsername(properties.getUsername());
            config.setPassword(properties.getPassword());
            config.setMaximumPoolSize(poolSize);

            return new CustomDataSource(config);

        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Failed to load DB driver: " + driver, e);

        } catch (SQLException e) {
            throw new IllegalStateException("Failed to create custom DataSource", e);
        }
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public JedisPool jedisPool(AppRedisProperties properties) {
        JedisPoolConfig config = new JedisPoolConfig();

        if (properties.getPool().getMaxTotal() != null) {
            config.setMaxTotal(properties.getPool().getMaxTotal());
        }
        if (properties.getPool().getMaxIdle() != null) {
            config.setMaxIdle(properties.getPool().getMaxIdle());
        }
        if (properties.getPool().getMinIdle() != null) {
            config.setMinIdle(properties.getPool().getMinIdle());
        }
        if (properties.getPool().getTestOnBorrow() != null) {
            config.setTestOnBorrow(properties.getPool().getTestOnBorrow());
        }

        int port = properties.getPort() != null ? properties.getPort() : 6379;
        int timeoutMs = properties.getTimeoutMs() != null ? properties.getTimeoutMs() : 2000;

        return new JedisPool(config, properties.getHost(), port, timeoutMs);
    }

    @Bean
    public HiddenHttpMethodFilter hiddenHttpMethodFilter() {
        return new HiddenHttpMethodFilter();
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder
                .featuresToEnable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .featuresToEnable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .modules(new JavaTimeModule());
    }
}
