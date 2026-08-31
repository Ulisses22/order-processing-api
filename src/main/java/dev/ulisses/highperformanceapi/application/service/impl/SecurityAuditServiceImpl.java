package dev.ulisses.highperformanceapi.application.service.impl;

import dev.ulisses.highperformanceapi.application.service.SecurityAuditService;
import dev.ulisses.highperformanceapi.domain.entity.AuditLog;
import dev.ulisses.highperformanceapi.domain.enums.AuditAction;
import dev.ulisses.highperformanceapi.domain.repository.AuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class SecurityAuditServiceImpl implements SecurityAuditService {

    private static final String SECURITY_ENTITY = "Security";

    private final AuditLogRepository auditLogRepository;

    public SecurityAuditServiceImpl(
            AuditLogRepository auditLogRepository) {

        this.auditLogRepository = auditLogRepository;
    }

    @Override
    public void record(
            AuditAction action,
            UUID entityId,
            String username,
            String details) {

        AuditLog auditLog = new AuditLog();

        auditLog.setEntityName(SECURITY_ENTITY);
        auditLog.setEntityId(entityId);
        auditLog.setAction(action);
        auditLog.setUsername(username);
        auditLog.setDetails(details);

        auditLogRepository.save(auditLog);
    }
}
