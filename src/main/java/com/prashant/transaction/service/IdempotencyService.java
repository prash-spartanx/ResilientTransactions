package com.prashant.transaction.service;

import com.prashant.transaction.model.IdempotencyRecord;
import com.prashant.transaction.model.IdempotencyResult;
import com.prashant.transaction.repository.IdempotencyRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class IdempotencyService {

    private final IdempotencyRepository idempotencyRepository;
    private final StringRedisTemplate redisTemplate;

    private static final Duration RESERVATION_TTL = Duration.ofMinutes(5);

    public IdempotencyService(
            IdempotencyRepository idempotencyRepository,
            StringRedisTemplate redisTemplate) {

        this.idempotencyRepository = idempotencyRepository;
        this.redisTemplate = redisTemplate;
    }

    public IdempotencyResult checkAndReserve(
            String idempotencyKey,
            String accountId,
            String destinationAccountId,
            double amount) {

        String redisKey = "idem:" + idempotencyKey;

        Boolean claimed = redisTemplate.opsForValue()
                .setIfAbsent(
                        redisKey,
                        "IN_PROGRESS",
                        RESERVATION_TTL
                );

        // This request successfully claimed the idempotency key.
        if (Boolean.TRUE.equals(claimed)) {
            return IdempotencyResult.PROCEED;
        }

        // Another request already claimed this idempotency key.
        IdempotencyRecord existingRecord =
                idempotencyRepository.findByKey(idempotencyKey);

        if (existingRecord == null) {
            return IdempotencyResult.IN_PROGRESS;
        }

        String currentRequestHash =
                calculateRequestHash(
                        accountId,
                        destinationAccountId,
                        amount
                );

        // Same idempotency key but different request.
        if (!existingRecord.getRequestHash().equals(currentRequestHash)) {
            return IdempotencyResult.CONFLICT;
        }

        // Same request was already successfully completed.
        if (existingRecord.getResultPayload() != null) {
            return IdempotencyResult.REPLAY;
        }

        // Same request is still being processed.
        return IdempotencyResult.IN_PROGRESS;
    }

    private String calculateRequestHash(
            String accountId,
            String destinationAccountId,
            double amount) {

        String input =
                accountId + destinationAccountId + amount;

        return Integer.toString(input.hashCode());
    }
}