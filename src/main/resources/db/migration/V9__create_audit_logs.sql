CREATE TABLE audit_logs
(
    id UUID NOT NULL,

    entity_name VARCHAR(100) NOT NULL,
    entity_id UUID NOT NULL,

    action VARCHAR(30) NOT NULL,
    username VARCHAR(100) NOT NULL,

    details TEXT,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL,

    CONSTRAINT pk_audit_logs
        PRIMARY KEY (id),

    CONSTRAINT chk_audit_log_action
        CHECK (
            action IN (
                       'CREATE',
                       'UPDATE',
                       'DELETE',
                       'LOGIN',
                       'LOGOUT'
                )
            )
);

CREATE INDEX idx_audit_logs_entity
    ON audit_logs(entity_name, entity_id);

CREATE INDEX idx_audit_logs_action
    ON audit_logs(action);

CREATE INDEX idx_audit_logs_created_at
    ON audit_logs(created_at);
