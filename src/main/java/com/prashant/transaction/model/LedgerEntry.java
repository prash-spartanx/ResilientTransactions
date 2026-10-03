package com.prashant.transaction.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_id", nullable = false, updatable = false)
    private UUID transactionId;

    @Column(name = "account_id", nullable = false, updatable = false)
    private Long accountId;

    @Column(name = "amount", nullable = false, updatable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "direction", nullable = false, updatable = false, length = 6)
    private LedgerDirection direction;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();


    public LedgerEntry(UUID transactionId, Long accountId, BigDecimal amount, LedgerDirection direction) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.amount = amount;
        this.direction = direction;
    }
    public Money getAmount() { return new Money(amount); }

}
