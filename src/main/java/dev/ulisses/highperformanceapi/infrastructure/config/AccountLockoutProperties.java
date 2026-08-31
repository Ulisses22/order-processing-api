package dev.ulisses.highperformanceapi.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "security.account-lockout")
public record AccountLockoutProperties(
        int maxFailedAttempts,
        long lockDurationSeconds
) {
}
