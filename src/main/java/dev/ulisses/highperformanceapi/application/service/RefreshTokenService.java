package dev.ulisses.highperformanceapi.application.service;

import dev.ulisses.highperformanceapi.domain.entity.User;

public interface RefreshTokenService {

    String create(User user);

    User validate(String refreshToken);

    String rotate(String refreshToken);

    void revoke(String refreshToken);
}
