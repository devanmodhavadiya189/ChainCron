ALTER TABLE users
    ADD COLUMN wallet_address VARCHAR(42);

CREATE UNIQUE INDEX idx_users_wallet_address
    ON users (wallet_address)
    WHERE wallet_address IS NOT NULL;
