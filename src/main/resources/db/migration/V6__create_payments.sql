

CREATE TABLE payments
(
    id UUID NOT NULL,

    order_id UUID NOT NULL,

    amount NUMERIC(19,2) NOT NULL,

    method VARCHAR(30) NOT NULL,
    status VARCHAR(30) NOT NULL,

    transaction_id VARCHAR(255),
    authorization_code VARCHAR(255),

    processed_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL,

    CONSTRAINT pk_payments
        PRIMARY KEY (id),

    CONSTRAINT fk_payments_order
        FOREIGN KEY (order_id)
            REFERENCES orders(id),

    CONSTRAINT chk_payment_amount
        CHECK (amount >= 0),

    CONSTRAINT chk_payment_method
        CHECK (
            method IN (
                       'CREDIT_CARD',
                       'DEBIT_CARD',
                       'PAYPAL',
                       'BANK_TRANSFER',
                       'PIX'
                )
            ),

    CONSTRAINT chk_payment_status
        CHECK (
            status IN (
                       'PENDING',
                       'AUTHORIZED',
                       'PAID',
                       'FAILED',
                       'CANCELLED',
                       'REFUNDED'
                )
            )
);

CREATE INDEX idx_payments_order
    ON payments(order_id);

CREATE INDEX idx_payments_status
    ON payments(status);

CREATE INDEX idx_payments_created_at
    ON payments(created_at);
