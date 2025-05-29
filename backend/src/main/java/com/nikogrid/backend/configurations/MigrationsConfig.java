package com.nikogrid.backend.configurations;

import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MigrationsConfig {

    // Redefine the flyway migration Bean otherwise when building for production with AoT,
    // flyway won't get properly disabled and CDS won't be able to progress because it
    // wants to connect to a data source.
    @Bean
    FlywayMigrationStrategy flywayMigrationStrategy() {
        return (flyway) -> {
            String flywayEnabled = System.getProperty("spring.flyway.enabled");
            if (flywayEnabled == null || Boolean.parseBoolean(flywayEnabled)) {
                flyway.migrate();
            }
        };
    }

}
