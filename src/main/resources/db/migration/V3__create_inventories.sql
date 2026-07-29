CREATE TABLE inventories
(
    id UUID NOT NULL,

    product_id UUID NOT NULL,

    available_quantity INTEGER NOT NULL,
    reserved_quantity INTEGER NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL,

    CONSTRAINT pk_inventories
        PRIMARY KEY (id),

    CONSTRAINT uk_inventory_product
        UNIQUE (product_id),

    CONSTRAINT fk_inventory_product
        FOREIGN KEY (product_id)
            REFERENCES products(id),

    CONSTRAINT chk_inventory_available_quantity
        CHECK (available_quantity >= 0),

    CONSTRAINT chk_inventory_reserved_quantity
        CHECK (reserved_quantity >= 0),

    CONSTRAINT chk_inventory_reserved_not_greater_than_available
        CHECK (reserved_quantity <= available_quantity)
);

CREATE INDEX idx_inventory_created_at
    ON inventories(created_at);
