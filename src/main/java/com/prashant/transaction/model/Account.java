package com.prashant.transaction.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "accounts")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "cached_balance", nullable = false, precision = 19, scale = 4)
    private BigDecimal cachedBalance;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();


    public Account(String ownerName, Money initialBalance) {
        this.ownerName = ownerName;
        this.cachedBalance = initialBalance.amount();
    }

    public Long getId() { return id; }
    public String getOwnerName() { return ownerName; }
    public Money getBalance() { return new Money(cachedBalance); }


    public void debit(Money amount) {
        Money current = getBalance();
        if (current.isLessThan(amount)) {
            throw new IllegalStateException("Insufficient funds for account ID: " + id);
        }
        this.cachedBalance = current.subtract(amount).amount();
        this.updatedAt = Instant.now();
    }

    public void credit(Money amount) {
        this.cachedBalance = getBalance().add(amount).amount();
        this.updatedAt = Instant.now();
    }
}
