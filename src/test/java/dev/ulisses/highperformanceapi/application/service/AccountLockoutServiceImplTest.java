package dev.ulisses.highperformanceapi.application.service;

import dev.ulisses.highperformanceapi.application.service.impl.AccountLockoutServiceImpl;
import dev.ulisses.highperformanceapi.domain.entity.User;
import dev.ulisses.highperformanceapi.domain.enums.AuditAction;
import dev.ulisses.highperformanceapi.domain.repository.UserRepository;
import dev.ulisses.highperformanceapi.infrastructure.config.AccountLockoutProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountLockoutServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AccountLockoutProperties properties;

    @Mock
    private SecurityAuditService securityAuditService;

    @InjectMocks
    private AccountLockoutServiceImpl accountLockoutService;

    @Test
    @DisplayName("Should record account locked audit event")
    void shouldRecordAccountLockedAuditEvent() {

        when(properties.maxFailedAttempts())
                .thenReturn(5);

        when(properties.lockDurationSeconds())
                .thenReturn(900L);

        User user = new User();
        user.setUsername("admin");
        user.setFailedLoginAttempts(4);

        when(userRepository.findByUsername("admin"))
                .thenReturn(Optional.of(user));

        accountLockoutService.handleFailedLogin("admin");

        assertEquals(5, user.getFailedLoginAttempts());
        assertNotNull(user.getLockedUntil());

        verify(securityAuditService).record(
                eq(AuditAction.ACCOUNT_LOCKED),
                eq(user.getId()),
                eq("admin"),
                eq("Account locked after maximum failed login attempts.")
        );
    }

    @Test
    @DisplayName("Should record account unlocked audit event")
    void shouldRecordAccountUnlockedAuditEvent() {

        User user = new User();
        user.setUsername("admin");
        user.setFailedLoginAttempts(5);
        user.setLockedUntil(
                Instant.now().minusSeconds(60)
        );

        boolean locked = accountLockoutService.isLocked(user);

        assertFalse(locked);
        assertEquals(0, user.getFailedLoginAttempts());
        assertNull(user.getLockedUntil());

        verify(securityAuditService).record(
                eq(AuditAction.ACCOUNT_UNLOCKED),
                eq(user.getId()),
                eq("admin"),
                eq("Account lock expired.")
        );
    }
}
