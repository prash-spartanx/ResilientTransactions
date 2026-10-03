-- V1__init_ledger_schema.sql

-- 1. ACCOUNTS TABLE
CREATE TABLE accounts (
    id             BIGSERIAL PRIMARY KEY,
    owner_name     VARCHAR(255) NOT NULL,
    cached_balance NUMERIC(19, 4) NOT NULL DEFAULT 0.0000,
    version        BIGINT NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    updated_at     TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

-- 2. LEDGER ENTRIES TABLE (APPEND-ONLY)
CREATE TABLE ledger_entries (
    id             BIGSERIAL PRIMARY KEY,
    transaction_id UUID NOT NULL,
    account_id     BIGINT NOT NULL REFERENCES accounts(id),
    amount         NUMERIC(19, 4) NOT NULL,
    direction      VARCHAR(6) NOT NULL CHECK (direction IN ('DEBIT', 'CREDIT')),
    created_at     TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE INDEX idx_ledger_entries_tx_id ON ledger_entries(transaction_id);
CREATE INDEX idx_ledger_entries_account_id ON ledger_entries(account_id);

-- Enforce append-only semantics at the database tier
CREATE OR REPLACE FUNCTION prevent_ledger_modification()
RETURNS TRIGGER AS $$
BEGIN
    RAISE EXCEPTION 'Modification or deletion of ledger entries is strictly prohibited.';
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_prevent_ledger_update_delete
BEFORE UPDATE OR DELETE ON ledger_entries
FOR EACH ROW EXECUTE FUNCTION prevent_ledger_modification();

-- 3. IDEMPOTENCY RECORDS TABLE
CREATE TABLE idempotency_records (
    id              BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(255) NOT NULL UNIQUE,
    request_hash    VARCHAR(64) NOT NULL,
    result_payload  TEXT NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);

CREATE INDEX idx_idempotency_key ON idempotency_records(idempotency_key);

-- V2__outbox_events.sql
-- Outbox Events Table for durable messaging pipeline

CREATE TABLE outbox_events (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    transaction_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id   VARCHAR(255) NOT NULL,
    type           VARCHAR(255) NOT NULL,
    payload        TEXT NOT NULL,
    status         VARCHAR(32) NOT NULL DEFAULT 'PENDING',
    created_at     TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp(),
    published_at   TIMESTAMPTZ
);


CREATE INDEX idx_outbox_status_created ON outbox_events(status, created_at);
--Processed Events Table for Tracking Consumed Events
CREATE TABLE processed_events (
    id             UUID PRIMARY KEY ,
    processed_at   TIMESTAMPTZ NOT NULL DEFAULT clock_timestamp()
);
