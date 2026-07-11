CREATE TABLE users (
    id                  BIGSERIAL       PRIMARY KEY,
    email               VARCHAR(320)    NOT NULL,
    google_sub          VARCHAR(255)    NOT NULL,
    credit_balance_wei  NUMERIC(38, 0)  NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT uq_users_email       UNIQUE (email),
    CONSTRAINT uq_users_google_sub  UNIQUE (google_sub),
    CONSTRAINT chk_users_balance    CHECK (credit_balance_wei >= 0)
);

CREATE INDEX idx_users_email ON users (email);
