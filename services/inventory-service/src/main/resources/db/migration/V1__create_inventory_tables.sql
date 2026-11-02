CREATE TABLE inventory (
                           id BIGSERIAL PRIMARY KEY,
                           product_id VARCHAR(100) NOT NULL UNIQUE,
                           available_quantity INTEGER NOT NULL DEFAULT 0,
                           reserved_quantity INTEGER NOT NULL DEFAULT 0,
                           version BIGINT NOT NULL DEFAULT 0,
                           created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                           CONSTRAINT chk_inventory_available
                               CHECK (available_quantity >= 0),

                           CONSTRAINT chk_inventory_reserved
                               CHECK (reserved_quantity >= 0)
);

CREATE TABLE inventory_reservation (
                                       id BIGSERIAL PRIMARY KEY,
                                       order_id VARCHAR(100) NOT NULL,
                                       product_id VARCHAR(100) NOT NULL,
                                       quantity INTEGER NOT NULL,
                                       status VARCHAR(30) NOT NULL,
                                       created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                       updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                       CONSTRAINT chk_reservation_quantity
                                           CHECK (quantity > 0),

                                       CONSTRAINT uq_order_product
                                           UNIQUE (order_id, product_id),

                                       CONSTRAINT fk_reservation_inventory
                                           FOREIGN KEY (product_id)
                                               REFERENCES inventory(product_id)
);