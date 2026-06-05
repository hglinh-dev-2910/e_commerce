CREATE TABLE products (
                          id              BIGSERIAL       PRIMARY KEY,
                          name            VARCHAR(200)    NOT NULL,
                          description     TEXT,
                          price           DECIMAL(10,2)   NOT NULL CHECK (price > 0),
                          stock_quantity  INT             NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
                          category_id     BIGINT          NOT NULL REFERENCES categories(id),
                          is_active       BOOLEAN         NOT NULL DEFAULT TRUE,
                          created_at      TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_price ON products(price);