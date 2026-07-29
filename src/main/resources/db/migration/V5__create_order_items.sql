CREATE TABLE order_items
(
    id UUID NOT NULL,

    order_id UUID NOT NULL,
    product_id UUID NOT NULL,

    product_sku VARCHAR(100) NOT NULL,
    product_name VARCHAR(255) NOT NULL,

    unit_price NUMERIC(19,2) NOT NULL,
    quantity INTEGER NOT NULL,
    subtotal NUMERIC(19,2) NOT NULL,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,
    version BIGINT NOT NULL,

    CONSTRAINT pk_order_items
        PRIMARY KEY (id),

    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
            REFERENCES orders(id),

    CONSTRAINT fk_order_items_product
        FOREIGN KEY (product_id)
            REFERENCES products(id),

    CONSTRAINT chk_order_item_unit_price
        CHECK (unit_price >= 0),

    CONSTRAINT chk_order_item_quantity
        CHECK (quantity > 0),

    CONSTRAINT chk_order_item_subtotal
        CHECK (subtotal >= 0)
);

CREATE INDEX idx_order_items_order
    ON order_items(order_id);

CREATE INDEX idx_order_items_product
    ON order_items(product_id);
