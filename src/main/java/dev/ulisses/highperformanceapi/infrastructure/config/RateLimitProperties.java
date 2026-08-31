package dev.ulisses.highperformanceapi.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.rate-limit")
public record RateLimitProperties(
        int capacity,
        int refillTokens,
        long refillDurationSeconds
) {
}
