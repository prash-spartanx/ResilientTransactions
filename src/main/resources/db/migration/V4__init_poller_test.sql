INSERT INTO outbox_events
    (id,
     transaction_id,
     aggregate_type,
     aggregate_id,
     type,
     payload,
     status,
     created_at,
     published_at)
SELECT
    gen_random_uuid(),
    gen_random_uuid(),
    'PAYMENT',
    gen_random_uuid(),
    'PAYMENT_SETTLED',
    json_build_object(
        'eventId', gen_random_uuid(),
        'transactionId', gen_random_uuid(),
        'sourceAccountId', 9,
        'destinationAccountId', 10,
        'amount', 100.00,
        'eventType', 'PAYMENT_SETTLED',
        'createdAt', now()
    )::text,
    'PENDING',
    now(),
    NULL
FROM generate_series(1, 100);