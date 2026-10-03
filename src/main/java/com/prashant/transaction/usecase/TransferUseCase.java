package com.prashant.transaction.usecase;

import com.prashant.transaction.model.IdempotencyRecord;
import com.prashant.transaction.model.IdempotencyResult;
import com.prashant.transaction.model.Money;
import com.prashant.transaction.repository.IdempotencyRepository;
import com.prashant.transaction.service.IdempotencyService;
import com.prashant.transaction.service.LedgerService;
import com.prashant.transaction.service.LockProvider;
import com.prashant.transaction.service.PaymentValidator;
import com.prashant.transaction.util.RequestHashGenerator;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TransferUseCase {
    private final LedgerService ledgerService;
    private final PaymentValidator paymentValidator;
    private final IdempotencyService idempotencyService;
    private final LockProvider lockProvider;
    private final IdempotencyRepository idempotencyRepository;
    public  TransferUseCase(LedgerService ledgerService, PaymentValidator paymentValidator
    , IdempotencyService idempotencyService, LockProvider lockProvider
    , IdempotencyRepository idempotencyRepository) {
        this.idempotencyRepository = idempotencyRepository;
        this.ledgerService = ledgerService;
        this.paymentValidator = paymentValidator;
        this.idempotencyService = idempotencyService;
        this.lockProvider = lockProvider;
    }
    @Transactional
    public void transfer(long sourceAccountId, long destinationAccountId, double amount ) {

        if (!paymentValidator.isValid(sourceAccountId, destinationAccountId, amount)) {
            throw new IllegalArgumentException("Invalid transfer request");
        }
        Money money = Money.of(amount);
        UUID transactionId = UUID.randomUUID();
        ledgerService.transfer(sourceAccountId, destinationAccountId,money, transactionId);
    }
    // Pessimistic locking version
    @Transactional
    public void transferPessimistic(long sourceAccountId, long destinationAccountId, double amount ) {
        if (!paymentValidator.isValidPessimistic(sourceAccountId, destinationAccountId, amount)) {
            throw new IllegalArgumentException("Invalid transfer request");
        }
        Money money = Money.of(amount);
        UUID transactionId = UUID.randomUUID();
        ledgerService.transferPessimistic(sourceAccountId, destinationAccountId,money, transactionId);
    }

    // Optimistic locking version
    @Transactional
    public void transferOptimistic(long sourceAccountId, long destinationAccountId, double amount ,String idempotencyKey) {
        if (!paymentValidator.isValidOptimistic(sourceAccountId, destinationAccountId, amount)) {
            throw new IllegalArgumentException("Invalid transfer request");
        }
        Money money = Money.of(amount);
        UUID transactionId = UUID.randomUUID();
        ledgerService.transferOptimisticWithRetry(sourceAccountId, destinationAccountId, money, transactionId,idempotencyKey);
    }
    public String transferOptimisticWithProtection(
            long sourceAccountId,
            long destinationAccountId,
            double amount,
            String idempotencyKey) {

        // 1. Validate request
        if (!paymentValidator.isValidOptimistic(
                sourceAccountId,
                destinationAccountId,
                amount)) {

            throw new IllegalArgumentException(
                    "Invalid transfer request");
        }

        // 2. Idempotency check
        IdempotencyResult result =
                idempotencyService.checkAndReserve(
                        idempotencyKey,
                        String.valueOf(sourceAccountId),
                        String.valueOf(destinationAccountId),
                        amount
                );

        switch (result) {

            case REPLAY:
                return idempotencyRepository
                        .findByKey(idempotencyKey)
                        .getResultPayload();

            case IN_PROGRESS:
                throw new IllegalStateException(
                        "This payment is already in progress");

            case CONFLICT:
                throw new IllegalStateException(
                        "Conflicting request for the same idempotency key");

            case PROCEED:
                break;
        }

        // 3. Generate account-pair lock key
        String redisKey =
                generateRedisKey(
                        sourceAccountId,
                        destinationAccountId
                );

        // 4. Acquire distributed lock
        if (!lockProvider.acquireLock(redisKey)) {
            throw new IllegalStateException(
                    "Could not acquire transfer lock");
        }

        try {

            // 5. Generate transaction ID
            Money money = Money.of(amount);
            UUID transactionId = UUID.randomUUID();

            // 6. Perform existing database transfer
            ledgerService.transferOptimisticWithRetry(
                    sourceAccountId,
                    destinationAccountId,
                    money,
                    transactionId,
                    idempotencyKey
            );


            // 8. Return same result that a retry will receive
            return transactionId.toString();

        } finally {

            // 9. Always release Redis lock
            lockProvider.releaseLock(redisKey);
        }
    }
    private String generateRedisKey(
            long sourceAccountId,
            long destinationAccountId) {

        long first = Math.min(sourceAccountId, destinationAccountId);
        long second = Math.max(sourceAccountId, destinationAccountId);

        return "transfer:" + first + ":" + second;
    }

}
