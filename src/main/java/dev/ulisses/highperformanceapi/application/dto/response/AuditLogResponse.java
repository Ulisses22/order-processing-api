package dev.ulisses.highperformanceapi.application.dto.response;

import dev.ulisses.highperformanceapi.domain.enums.AuditAction;

import java.time.Instant;
import java.util.UUID;

public record AuditLogResponse(

        UUID id,

        String entityName,

        UUID entityId,

        AuditAction action,

        String performedBy,

        Instant createdAt

) {
}
