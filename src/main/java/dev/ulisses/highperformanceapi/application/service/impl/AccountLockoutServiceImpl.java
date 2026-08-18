package dev.ulisses.highperformanceapi.application.service.impl;

import dev.ulisses.highperformanceapi.application.service.AccountLockoutService;
import dev.ulisses.highperformanceapi.domain.entity.User;
import dev.ulisses.highperformanceapi.domain.repository.UserRepository;
import dev.ulisses.highperformanceapi.infrastructure.config.AccountLockoutProperties;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class AccountLockoutServiceImpl implements AccountLockoutService {

    private final UserRepository userRepository;
    private final AccountLockoutProperties properties;

    public AccountLockoutServiceImpl(
            UserRepository userRepository,
            AccountLockoutProperties properties) {

        this.userRepository = userRepository;
        this.properties = properties;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleFailedLogin(String username) {

        userRepository.findByUsername(username)
                .ifPresent(user -> {

                    int attempts = user.getFailedLoginAttempts() + 1;

                    user.setFailedLoginAttempts(attempts);

                    if (attempts >= properties.maxFailedAttempts()) {
                        user.setLockedUntil(
                                Instant.now()
                                        .plusSeconds(
                                                properties.lockDurationSeconds()
                                        )
                        );
                    }

                    userRepository.save(user);
                });
    }

    @Override
    @Transactional
    public void handleSuccessfulLogin(User user) {

        if (user.getFailedLoginAttempts() > 0
                || user.getLockedUntil() != null) {

            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);

            userRepository.save(user);
        }
    }

    @Override
    @Transactional
    public boolean isLocked(User user) {

        if (user.getLockedUntil() == null) {
            return false;
        }

        if (user.getLockedUntil().isAfter(Instant.now())) {
            return true;
        }

        user.setLockedUntil(null);
        user.setFailedLoginAttempts(0);

        userRepository.save(user);

        return false;
    }
}
