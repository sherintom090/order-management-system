CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        customer_id VARCHAR(100) NOT NULL,
                        status VARCHAR(30) NOT NULL,
                        total_amount NUMERIC(12, 2) NOT NULL,
                        currency VARCHAR(3) NOT NULL DEFAULT 'USD',
                        created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);