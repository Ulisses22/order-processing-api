package dev.ulisses.highperformanceapi.application.service.impl;

import dev.ulisses.highperformanceapi.application.service.RefreshTokenService;
import dev.ulisses.highperformanceapi.domain.entity.RefreshToken;
import dev.ulisses.highperformanceapi.domain.entity.User;
import dev.ulisses.highperformanceapi.domain.repository.RefreshTokenRepository;
import dev.ulisses.highperformanceapi.infrastructure.config.JwtProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;

@Service
@Transactional
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private static final int TOKEN_BYTES = 32;
    private final JwtProperties jwtProperties;

    private final RefreshTokenRepository refreshTokenRepository;

    private final SecureRandom secureRandom = new SecureRandom();

    public RefreshTokenServiceImpl(
            RefreshTokenRepository refreshTokenRepository,
            JwtProperties jwtProperties) {

        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtProperties = jwtProperties;
    }

    @Override
    public String create(User user) {

        String rawToken = generateToken();

        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(user);
        refreshToken.setTokenHash(hashToken(rawToken));
        refreshToken.setExpiresAt(
                Instant.now().plusSeconds(jwtProperties.refreshExpiration())
        );

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Override
    @Transactional(readOnly = true)
    public User validate(String rawToken) {

        RefreshToken refreshToken = findToken(rawToken);

        if (refreshToken.getRevokedAt() != null) {
            throw new IllegalArgumentException("Refresh token has been revoked.");
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token has expired.");
        }

        return refreshToken.getUser();
    }

    @Override
    public String rotate(String rawToken) {

        RefreshToken refreshToken = findToken(rawToken);

        if (refreshToken.getRevokedAt() != null) {
            throw new IllegalArgumentException("Refresh token has been revoked.");
        }

        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            throw new IllegalArgumentException("Refresh token has expired.");
        }

        User user = refreshToken.getUser();

        refreshToken.setRevokedAt(Instant.now());
        refreshTokenRepository.save(refreshToken);

        return create(user);
    }

    @Override
    public void revoke(String rawToken) {

        RefreshToken refreshToken = findToken(rawToken);

        if (refreshToken.getRevokedAt() == null) {
            refreshToken.setRevokedAt(Instant.now());
            refreshTokenRepository.save(refreshToken);
        }
    }

    private RefreshToken findToken(String rawToken) {

        String tokenHash = hashToken(rawToken);

        return refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() ->
                        new IllegalArgumentException("Invalid refresh token.")
                );
    }

    private String generateToken() {

        byte[] tokenBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(tokenBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(tokenBytes);
    }

    private String hashToken(String token) {

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                    token.getBytes(StandardCharsets.UTF_8)
            );

            return Base64.getUrlEncoder()
                    .withoutPadding()
                    .encodeToString(hash);

        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "SHA-256 algorithm is not available.",
                    ex
            );
        }
    }
}
