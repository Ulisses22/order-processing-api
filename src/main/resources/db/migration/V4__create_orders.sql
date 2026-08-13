CREATE TABLE orders
(
    id UUID NOT NULL,

    order_number VARCHAR(50) NOT NULL,

    customer_id UUID NOT NULL,

    total_amount NUMERIC(19,2) NOT NULL,

    status VARCHAR(30) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL,

    CONSTRAINT pk_orders
        PRIMARY KEY (id),

    CONSTRAINT uk_orders_order_number
        UNIQUE (order_number),

    CONSTRAINT fk_orders_customer
        FOREIGN KEY (customer_id)
            REFERENCES customers(id),

    CONSTRAINT chk_order_total_amount
        CHECK (total_amount >= 0),

    CONSTRAINT chk_order_status
        CHECK (
            status IN (
                       'PENDING',
                       'VALIDATING',
                       'CONFIRMED',
                       'PAYMENT_PENDING',
                       'PAID',
                       'PROCESSING',
                       'SHIPPED',
                       'DELIVERED',
                       'CANCELLED',
                       'REFUNDED'
                )
            )
);

CREATE INDEX idx_order_customer_created_at
    ON orders(customer_id, created_at DESC);

CREATE INDEX idx_order_status
    ON orders(status);
