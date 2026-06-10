CREATE TABLE invalidated_tokens (
    id VARCHAR(36) PRIMARY KEY,
    expiry_time TIMESTAMP NOT NULL
);
