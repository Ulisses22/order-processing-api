package dev.ulisses.highperformanceapi.domain.entity;

import dev.ulisses.highperformanceapi.domain.enums.AuditAction;
import jakarta.persistence.*;

import java.util.UUID;

@Table(
        name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_entity", columnList = "entity_name"),
                @Index(name = "idx_audit_entity_id", columnList = "entity_id"),
                @Index(name = "idx_audit_action", columnList = "action"),
                @Index(name = "idx_audit_created_at", columnList = "created_at")
        }
)
@Entity
public class AuditLog extends BaseEntity {

    // This tells us which table/entity was affected
    @Column(name = "entity_name", nullable = false, length = 100)
    private String entityName;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AuditAction action;

    @Column(nullable = false, length = 100)
    private String username;

    @Column(length = 5000)
    private String details;

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }
}
