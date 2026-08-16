package dev.ulisses.highperformanceapi.application.dto.response;

public record LoginResponse(
        String accessToken,
        String tokenType,
        long expiresIn
) {
}
