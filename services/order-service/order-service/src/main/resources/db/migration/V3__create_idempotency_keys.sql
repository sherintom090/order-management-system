CREATE TABLE idempotency_keys (
                                  id BIGSERIAL PRIMARY KEY,
                                  idempotency_key VARCHAR(100) NOT NULL UNIQUE,
                                  order_id BIGINT NOT NULL,
                                  created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_idempotency_order
                                      FOREIGN KEY (order_id)
                                          REFERENCES orders(id)
);