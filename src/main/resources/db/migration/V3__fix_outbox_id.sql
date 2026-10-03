-- V3__fix_outbox_ids.sql
-- Align schema with Java model (String IDs instead of UUID)

-- 1. Change outbox_events.id from UUID to VARCHAR
ALTER TABLE outbox_events
    ALTER COLUMN id DROP DEFAULT,
    ALTER COLUMN id TYPE VARCHAR(255) USING id::text;

-- 2. Change processed_events.id from UUID to VARCHAR
ALTER TABLE processed_events
    ALTER COLUMN id TYPE VARCHAR(255) USING id::text;

-- Optional: if you want auto-generated IDs, remove DEFAULT gen_random_uuid()
-- and handle ID assignment in Java code (e.g., UUID.randomUUID().toString()).
