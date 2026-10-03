package com.prashant.transaction.service;

import com.prashant.transaction.model.Account;
import com.prashant.transaction.model.LedgerDirection;
import com.prashant.transaction.model.LedgerEntry;
import com.prashant.transaction.model.Money;
import com.prashant.transaction.repository.AccountRepository;
import com.prashant.transaction.repository.LedgerEntryRepository;
import jakarta.persistence.OptimisticLockException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
@Service
public class LedgerService {
    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final OptimisticTransferExecutor optimisticTransferExecutor;

    public LedgerService(AccountRepository accountRepository,
                         LedgerEntryRepository ledgerEntryRepository,
                         OptimisticTransferExecutor optimisticTransferExecutor) {
        this.accountRepository = accountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.optimisticTransferExecutor = optimisticTransferExecutor;
    }

    public List<LedgerEntry> transfer(long sourceAccountId,
                                      long destinationAccountId,
                                      Money money,
                                      UUID transactionId) {
        Account source = accountRepository.findById(sourceAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Source account not found"));
        Account destination = accountRepository.findById(destinationAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Destination account not found"));

        // Domain logic
        source.debit(money);
        destination.credit(money);

        // Persistence objects (new instances!)
        LedgerEntry debitEntry = new LedgerEntry(transactionId, sourceAccountId, money.amount(), LedgerDirection.DEBIT);
        LedgerEntry creditEntry = new LedgerEntry(transactionId, destinationAccountId, money.amount(), LedgerDirection.CREDIT);

        ledgerEntryRepository.save(debitEntry);
        ledgerEntryRepository.save(creditEntry);

        return List.of(debitEntry, creditEntry);
    }
    // Pessimistic locking version
    @Transactional
    public List<LedgerEntry> transferPessimistic(long sourceAccountId,
                                                 long destinationAccountId,
                                                 Money money,
                                                 UUID transactionId) {
        Optional<Account> source = accountRepository.findByIdForUpdate(sourceAccountId);
        Optional<Account> destination = accountRepository.findByIdForUpdate(destinationAccountId);


        // Domain logic
        source.get().debit(money);
        destination.get().credit(money);

        LedgerEntry debitEntry = new LedgerEntry(transactionId, sourceAccountId, money.amount(), LedgerDirection.DEBIT);
        LedgerEntry creditEntry = new LedgerEntry(transactionId, destinationAccountId, money.amount(), LedgerDirection.CREDIT);

        ledgerEntryRepository.save(debitEntry);
        ledgerEntryRepository.save(creditEntry);

        return List.of(debitEntry, creditEntry);

    }
    // Optimistic locking version
    public List<LedgerEntry> transferOptimisticWithRetry(long sourceAccountId, long destinationAccountId, Money money, UUID transactionId , String idempotencyKey) {
        int maxRetries = 10; // Under high contention, 10 threads need more retry room
        long backoffMillis = 50;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                // Call via injected Spring bean so Spring AOP creates a NEW transaction!
                return optimisticTransferExecutor.executeOptimisticTransfer(sourceAccountId, destinationAccountId, money, transactionId , idempotencyKey);
            } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                if (attempt == maxRetries) {
                    throw e;
                }
                try {
                    // Inside your retry catch block
                    long jitter = java.util.concurrent.ThreadLocalRandom.current().nextLong(10, 50);
                    Thread.sleep((backoffMillis * attempt) + jitter);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
            }
        }
        throw new IllegalStateException("Max retries exceeded");
    }
}
