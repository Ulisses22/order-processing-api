package dev.ulisses.highperformanceapi.application.service;

import dev.ulisses.highperformanceapi.domain.entity.User;

public interface AccountLockoutService {

    void handleFailedLogin(String username);

    void handleSuccessfulLogin(User user);

    boolean isLocked(User user);
}
