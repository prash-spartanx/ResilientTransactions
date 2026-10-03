package com.prashant.transaction;

import com.prashant.transaction.model.Account;
import com.prashant.transaction.model.LedgerDirection;
import com.prashant.transaction.model.LedgerEntry;
import com.prashant.transaction.model.Money;
import com.prashant.transaction.repository.AccountRepository;
import com.prashant.transaction.repository.LedgerEntryRepository;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
class Day1LedgerVerificationTest {

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private LedgerEntryRepository ledgerEntryRepository;

    @Test
    @Transactional
    @DisplayName("Verify double-entry zero-sum invariant across matched debit and credit entries")
    void verifyDoubleEntryZeroSumInvariant() {
        // 1. Setup accounts
        Account source = accountRepository.save(new Account("Alice", Money.of(1000.00)));
        Account destination = accountRepository.save(new Account("Bob", Money.of(500.00)));

        UUID txId = UUID.randomUUID();
        Money transferAmount = Money.of(200.00);

        // 2. Perform posting logic
        source.debit(transferAmount);
        destination.credit(transferAmount);

        accountRepository.save(source);
        accountRepository.save(destination);

        LedgerEntry debitEntry = new LedgerEntry(txId, source.getId(), transferAmount.amount(), LedgerDirection.DEBIT);
        LedgerEntry creditEntry = new LedgerEntry(txId, destination.getId(), transferAmount.amount(), LedgerDirection.CREDIT);

        ledgerEntryRepository.save(debitEntry);
        ledgerEntryRepository.save(creditEntry);

        // 3. Assert zero-sum balance invariant for transaction
        List<LedgerEntry> entries = ledgerEntryRepository.findByTransactionId(txId);
        assertThat(entries).hasSize(2);

        BigDecimal deltaSum = entries.stream()
                .map(entry -> entry.getDirection() == LedgerDirection.DEBIT
                        ? entry.getAmount().amount().negate()
                        : entry.getAmount().amount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        assertThat(deltaSum.compareTo(BigDecimal.ZERO.setScale(Money.SCALE)))
                .isEqualTo(0);
    }
}