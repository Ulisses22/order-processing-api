package dev.ulisses.highperformanceapi.application.service;

import dev.ulisses.highperformanceapi.application.service.impl.SecurityAuditServiceImpl;
import dev.ulisses.highperformanceapi.domain.entity.AuditLog;
import dev.ulisses.highperformanceapi.domain.enums.AuditAction;
import dev.ulisses.highperformanceapi.domain.repository.AuditLogRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.ArgumentCaptor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@ExtendWith(MockitoExtension.class)
class SecurityAuditServiceImplTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @InjectMocks
    private SecurityAuditServiceImpl securityAuditService;

    @Test
    @DisplayName("Should record login success audit event")
    void shouldRecordLoginSuccess() {

        UUID userId = UUID.randomUUID();

        securityAuditService.record(
                AuditAction.LOGIN_SUCCESS,
                userId,
                "admin",
                "User logged in successfully."
        );

        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(AuditLog.class);

        verify(auditLogRepository).save(captor.capture());

        AuditLog auditLog = captor.getValue();

        assertEquals("Security", auditLog.getEntityName());
        assertEquals(userId, auditLog.getEntityId());
        assertEquals(AuditAction.LOGIN_SUCCESS, auditLog.getAction());
        assertEquals("admin", auditLog.getUsername());
        assertEquals(
                "User logged in successfully.",
                auditLog.getDetails()
        );
    }

    @Test
    @DisplayName("Should record login failure audit event")
    void shouldRecordLoginFailure() {

        UUID userId = UUID.randomUUID();

        securityAuditService.record(
                AuditAction.LOGIN_FAILURE,
                userId,
                "admin",
                "Invalid credentials."
        );

        ArgumentCaptor<AuditLog> captor =
                ArgumentCaptor.forClass(AuditLog.class);

        verify(auditLogRepository).save(captor.capture());

        AuditLog auditLog = captor.getValue();

        assertEquals("Security", auditLog.getEntityName());
        assertEquals(userId, auditLog.getEntityId());
        assertEquals(AuditAction.LOGIN_FAILURE, auditLog.getAction());
        assertEquals("admin", auditLog.getUsername());
        assertEquals("Invalid credentials.", auditLog.getDetails());
    }
}
