package com.nikogrid.backend;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
@ConditionalOnProperty(prefix = "testcontainers", name = "enabled", matchIfMissing = true, havingValue = "true")
public class TestcontainersConfiguration {
    DockerImageName postgresPostgis = DockerImageName.parse("postgis/postgis:17-3.5-alpine").asCompatibleSubstituteFor("postgres");

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgresContainer() {
        final PostgreSQLContainer<?> container = new PostgreSQLContainer<>(postgresPostgis);
        container.withInitScript("dev-db-init.sql");
        return container;
    }

}
