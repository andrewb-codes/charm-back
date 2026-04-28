package ru.andrewb.charm.back.integration;

import org.springframework.context.annotation.Import;

@Import(IntegrationTestContainersConfig.class)
public abstract class AbstractIntegrationTest {
}
