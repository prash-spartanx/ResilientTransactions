INSERT INTO outbox_events
(
    id,
    transaction_id,
    aggregate_type,
    aggregate_id,
    type,
    payload,
    status,
    created_at,
    published_at
)
VALUES
(
    gen_random_uuid()::text,
    gen_random_uuid()::text,
    'PAYMENT',
    gen_random_uuid()::text,
    'PAYMENT_SETTLED',
    json_build_object(
        'eventId', gen_random_uuid()::text,
        'transactionId', gen_random_uuid()::text,
        'sourceAccountId', 9,
        'destinationAccountId', 10,
        'amount', 100.00,
        'eventType', 'PAYMENT_SETTLED',
        'createdAt', now()
    )::text,
    'PENDING',
    now(),
    NULL
),
(
    gen_random_uuid()::text,
    gen_random_uuid()::text,
    'PAYMENT',
    gen_random_uuid()::text,
    'PAYMENT_SETTLED',
    json_build_object(
        'eventId', gen_random_uuid()::text,
        'transactionId', gen_random_uuid()::text,
        'sourceAccountId', 9,
        'destinationAccountId', 10,
        'amount', 100.00,
        'eventType', 'PAYMENT_SETTLED',
        'createdAt', now()
    )::text,
    'PENDING',
    now(),
    NULL
),
(
    gen_random_uuid()::text,
    gen_random_uuid()::text,
    'PAYMENT',
    gen_random_uuid()::text,
    'PAYMENT_SETTLED',
    json_build_object(
        'eventId', gen_random_uuid()::text,
        'transactionId', gen_random_uuid()::text,
        'sourceAccountId', 9,
        'destinationAccountId', 10,
        'amount', 100.00,
        'eventType', 'PAYMENT_SETTLED',
        'createdAt', now()
    )::text,
    'PENDING',
    now(),
    NULL
),
(
    gen_random_uuid()::text,
    gen_random_uuid()::text,
    'PAYMENT',
    gen_random_uuid()::text,
    'PAYMENT_SETTLED',
    json_build_object(
        'eventId', gen_random_uuid()::text,
        'transactionId', gen_random_uuid()::text,
        'sourceAccountId', 9,
        'destinationAccountId', 10,
        'amount', 100.00,
        'eventType', 'PAYMENT_SETTLED',
        'createdAt', now()
    )::text,
    'PENDING',
    now(),
    NULL
),
(
    gen_random_uuid()::text,
    gen_random_uuid()::text,
    'PAYMENT',
    gen_random_uuid()::text,
    'PAYMENT_SETTLED',
    json_build_object(
        'eventId', gen_random_uuid()::text,
        'transactionId', gen_random_uuid()::text,
        'sourceAccountId', 9,
        'destinationAccountId', 10,
        'amount', 100.00,
        'eventType', 'PAYMENT_SETTLED',
        'createdAt', now()
    )::text,
    'PENDING',
    now(),
    NULL
);