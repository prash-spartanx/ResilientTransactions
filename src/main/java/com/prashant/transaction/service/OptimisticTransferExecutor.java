package com.prashant.transaction.service;

import com.prashant.transaction.model.*;
import com.prashant.transaction.repository.AccountRepository;
import com.prashant.transaction.repository.IdempotencyRepository;
import com.prashant.transaction.repository.LedgerEntryRepository;
import com.prashant.transaction.util.RequestHashGenerator;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;
@Component
public class OptimisticTransferExecutor {

    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final IdempotencyRepository idempotencyRepository;
    private final EventPublisher eventPublisher;
    public OptimisticTransferExecutor(AccountRepository accountRepository,
                                      LedgerEntryRepository ledgerEntryRepository , IdempotencyRepository idempotencyRepository,
    EventPublisher eventPublisher) {
        this.accountRepository = accountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public List<LedgerEntry> executeOptimisticTransfer(long sourceAccountId, long destinationAccountId, Money money, UUID transactionId , String idempotencyKey) {
        // Enforce deterministic ID ordering (smaller ID loaded first)
        long lowerId = Math.min(sourceAccountId, destinationAccountId);
        long higherId = Math.max(sourceAccountId, destinationAccountId);

        Account lowerAccount = accountRepository.findById(lowerId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + lowerId));
        Account higherAccount = accountRepository.findById(higherId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found: " + higherId));

        // Re-assign role references based on original request parameters
        Account source = (sourceAccountId == lowerId) ? lowerAccount : higherAccount;
        Account destination = (destinationAccountId == lowerId) ? lowerAccount : higherAccount;

        // Perform domain logic
        source.debit(money);
        destination.credit(money);

        // Create persistence objects
        LedgerEntry debitEntry = new LedgerEntry(transactionId, sourceAccountId, money.amount(), LedgerDirection.DEBIT);
        LedgerEntry creditEntry = new LedgerEntry(transactionId, destinationAccountId, money.amount(), LedgerDirection.CREDIT);

        ledgerEntryRepository.save(debitEntry);
        ledgerEntryRepository.save(creditEntry);

        // save idempotencyRecord
        IdempotencyRecord record = new IdempotencyRecord();
        record.setIdempotencyKey(idempotencyKey);
        // convert bigDecimal to double

        record.setRequestHash(RequestHashGenerator.generate(String.valueOf(sourceAccountId), String.valueOf(destinationAccountId), money.amount().doubleValue()));
        record.setResultPayload(transactionId.toString());
        record.setCreatedAt(LocalDateTime.now());
        idempotencyRepository.save(record);
        // Publish outbox event
        OutboxEvent event = new OutboxEvent();
        String eventId = UUID.randomUUID().toString();
        event.setId(eventId);
        event.setTransactionId(transactionId.toString());
        event.setType("PAYMENT_SETTLED");
        event.setAggregateId(transactionId.toString());
        event.setAggregateType("PAYMENT");
        event.setStatus("PENDING");
        event.setPublishedAt(null);
        event.setPayload("{"
                + "\"eventId\": \"" + eventId + "\", "
                + "\"transactionId\": \"" + transactionId + "\", "
                + "\"sourceAccountId\": \"" + sourceAccountId + "\", "
                + "\"destinationAccountId\": \"" + destinationAccountId + "\", "
                + "\"amount\": " + money.amount() + ", "
                + "\"eventType\": \"PAYMENT_SETTLED\""
                + "}");
        eventPublisher.publish(eventId ,event.getTransactionId(), event.getAggregateType(), event.getAggregateId(), event.getType(), event.getPayload());


        // Force immediate DB version check
        accountRepository.flush();

        return List.of(debitEntry, creditEntry);
    }
}