CREATE TABLE customers
(
    id UUID NOT NULL,

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(20),

    status VARCHAR(20) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL,

    CONSTRAINT pk_customers
        PRIMARY KEY (id),

    CONSTRAINT uk_customer_email
        UNIQUE (email),

    CONSTRAINT chk_customer_status
        CHECK (status IN (
                          'ACTIVE',
                          'INACTIVE',
                          'BLOCKED'
            ))
);

CREATE INDEX idx_customer_status
    ON customers(status);

CREATE INDEX idx_customer_created_at
    ON customers(created_at);
