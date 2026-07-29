
CREATE TABLE shipments
(
    id UUID NOT NULL,

    order_id UUID NOT NULL,

    carrier VARCHAR(100) NOT NULL,
    tracking_number VARCHAR(255) NOT NULL,

    status VARCHAR(30) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL,

    CONSTRAINT pk_shipments
        PRIMARY KEY (id),

    CONSTRAINT uk_shipments_order
        UNIQUE (order_id),

    CONSTRAINT uk_shipments_tracking_number
        UNIQUE (tracking_number),

    CONSTRAINT fk_shipments_order
        FOREIGN KEY (order_id)
            REFERENCES orders(id),

    CONSTRAINT chk_shipment_status
        CHECK (
            status IN (
                       'PENDING',
                       'PREPARING',
                       'SHIPPED',
                       'IN_TRANSIT',
                       'DELIVERED',
                       'RETURNED',
                       'CANCELLED'
                )
            )
);

CREATE INDEX idx_shipments_status
    ON shipments(status);

CREATE INDEX idx_shipments_created_at
    ON shipments(created_at);
