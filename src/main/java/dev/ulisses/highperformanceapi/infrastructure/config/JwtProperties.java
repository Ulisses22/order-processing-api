package dev.ulisses.highperformanceapi.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        String secret,
        long expiration,
        long refreshExpiration
) {
}
