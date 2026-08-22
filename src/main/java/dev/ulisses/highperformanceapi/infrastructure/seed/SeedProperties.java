package dev.ulisses.highperformanceapi.infrastructure.seed;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.seed.performance")
public record SeedProperties(
        boolean enabled,
        int customers,
        int products,
        int batchSize
) {
}
