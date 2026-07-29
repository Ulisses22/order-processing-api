CREATE TABLE products
(
    id UUID NOT NULL,

    sku VARCHAR(100) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(19,2) NOT NULL,

    status VARCHAR(20) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL,

    CONSTRAINT pk_products
        PRIMARY KEY (id),

    CONSTRAINT uk_product_sku
        UNIQUE (sku),

    CONSTRAINT chk_product_price
        CHECK (price >= 0),

    CONSTRAINT chk_product_status
        CHECK (status IN (
                          'ACTIVE',
                          'INACTIVE',
                          'DISCONTINUED'
            ))
);

CREATE INDEX idx_product_name
    ON products(name);

CREATE INDEX idx_product_status
    ON products(status);

CREATE INDEX idx_product_created_at
    ON products(created_at);
