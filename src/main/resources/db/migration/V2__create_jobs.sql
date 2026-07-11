CREATE TYPE job_status AS ENUM (
    'PENDING',
    'QUEUED',
    'EXECUTING',
    'SUBMITTED',
    'CONFIRMED',
    'FAILED'
);

CREATE TABLE jobs (
    id                      BIGSERIAL       PRIMARY KEY,
    user_id                 BIGINT          NOT NULL,
    contract_address        VARCHAR(42)     NOT NULL,
    function_sig            VARCHAR(512)    NOT NULL,
    encoded_calldata        BYTEA           NOT NULL,
    scheduled_at            TIMESTAMPTZ     NOT NULL,
    user_timezone           VARCHAR(64)     NOT NULL,
    status                  job_status      NOT NULL DEFAULT 'PENDING',
    tx_hash                 VARCHAR(66),
    gas_limit               NUMERIC(38, 0)  NOT NULL,
    gas_used                NUMERIC(38, 0),
    max_fee_per_gas         NUMERIC(38, 0)  NOT NULL,
    max_priority_fee_per_gas NUMERIC(38, 0) NOT NULL,
    charged_wei             NUMERIC(38, 0),
    attempt_count           INTEGER         NOT NULL DEFAULT 0,
    assigned_slot           BIGINT          NOT NULL,
    revert_reason           TEXT,
    block_number            BIGINT,
    created_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT fk_jobs_user FOREIGN KEY (user_id) REFERENCES users (id)
);

CREATE INDEX idx_jobs_user_id         ON jobs (user_id);
CREATE INDEX idx_jobs_status          ON jobs (status);
CREATE INDEX idx_jobs_assigned_slot   ON jobs (assigned_slot) WHERE status = 'PENDING';
CREATE INDEX idx_jobs_updated_at      ON jobs (updated_at)    WHERE status = 'EXECUTING';
CREATE INDEX idx_jobs_tx_hash         ON jobs (tx_hash)       WHERE tx_hash IS NOT NULL;
