
CREATE TABLE notifications
(
    id UUID NOT NULL,

    customer_id UUID NOT NULL,

    type VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,

    subject VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL,

    CONSTRAINT pk_notifications
        PRIMARY KEY (id),

    CONSTRAINT fk_notifications_customer
        FOREIGN KEY (customer_id)
            REFERENCES customers(id),

    CONSTRAINT chk_notification_type
        CHECK (
            type IN (
                     'EMAIL',
                     'SMS',
                     'PUSH'
                )
            ),

    CONSTRAINT chk_notification_status
        CHECK (
            status IN (
                       'PENDING',
                       'SENT',
                       'FAILED'
                )
            )
);

CREATE INDEX idx_notifications_customer_created_at
    ON notifications(customer_id, created_at DESC);

CREATE INDEX idx_notifications_status
    ON notifications(status);

