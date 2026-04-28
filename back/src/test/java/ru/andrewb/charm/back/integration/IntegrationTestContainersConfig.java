package ru.andrewb.charm.back.integration;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class IntegrationTestContainersConfig {

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:17"))
                .withDatabaseName("charm")
                .withUsername("charm")
                .withPassword("charmpass");
    }

    @Bean
    @ServiceConnection("redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("redis:7"))
                .withExposedPorts(6379);
    }

    @Bean
    DynamicPropertyRegistrar integrationProperties() {
        return registry -> {
            registry.add("app.content.base-path", () -> System.getProperty("java.io.tmpdir") + "/charm-it-content");
            registry.add("app.jwt.secret", () -> "12345678901234567890123456789012");
        };
    }
}
