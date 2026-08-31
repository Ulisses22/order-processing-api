package dev.ulisses.highperformanceapi.application.service;

import dev.ulisses.highperformanceapi.domain.enums.AuditAction;

import java.util.UUID;

public interface SecurityAuditService {

    void record(
            AuditAction action,
            UUID entityId,
            String username,
            String details
    );
}
