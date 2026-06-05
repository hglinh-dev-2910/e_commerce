CREATE TABLE categories (
                            id          BIGSERIAL     PRIMARY KEY,
                            name        VARCHAR(100)  NOT NULL UNIQUE,
                            description TEXT,
                            is_active   BOOLEAN       NOT NULL DEFAULT TRUE
);
