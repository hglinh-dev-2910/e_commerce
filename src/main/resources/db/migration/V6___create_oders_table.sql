CREATE TABLE orders (
                        id BIGSERIAL PRIMARY KEY,
                        user_id BIGINT NOT NULL REFERENCES users(id),
                        status VARCHAR(50) NOT NULL,
                        total_amount DECIMAL(10,2) NOT NULL,
                        note TEXT,
                        created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE order_items (
                             id BIGSERIAL PRIMARY KEY,
                             order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
                             product_id BIGINT NOT NULL REFERENCES products(id),
                             quantity INT NOT NULL CHECK (quantity >= 1),
                             unit_price DECIMAL(10,2) NOT NULL
);

CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_order_items_order_id ON order_items(order_id);
