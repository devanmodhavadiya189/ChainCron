CREATE TYPE credit_transaction_type AS ENUM (
    'DEPOSIT',
    'CHARGE',
    'REFUND'
);

CREATE TABLE credit_transactions (
    id              BIGSERIAL                   PRIMARY KEY,
    user_id         BIGINT                      NOT NULL,
    job_id          BIGINT,
    type            credit_transaction_type     NOT NULL,
    amount_wei      NUMERIC(38, 0)              NOT NULL,
    tx_hash         VARCHAR(66),
    note            TEXT,
    created_at      TIMESTAMPTZ                 NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_ct_user   FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_ct_job    FOREIGN KEY (job_id)  REFERENCES jobs  (id),
    CONSTRAINT chk_ct_amount CHECK (amount_wei > 0)
);

CREATE INDEX idx_ct_user_id     ON credit_transactions (user_id);
CREATE INDEX idx_ct_job_id      ON credit_transactions (job_id) WHERE job_id IS NOT NULL;
CREATE INDEX idx_ct_created_at  ON credit_transactions (created_at);
