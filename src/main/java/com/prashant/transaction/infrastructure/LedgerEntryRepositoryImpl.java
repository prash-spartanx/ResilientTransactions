package com.prashant.transaction.infrastructure;

import com.prashant.transaction.model.LedgerEntry;
import com.prashant.transaction.repository.LedgerEntryRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class LedgerEntryRepositoryImpl implements LedgerEntryRepository {

    private final SpringDataLedgerEntryRepository jpaRepository;

    public LedgerEntryRepositoryImpl(SpringDataLedgerEntryRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public LedgerEntry save(LedgerEntry entry) {
        return jpaRepository.save(entry);
    }

    @Override
    public List<LedgerEntry> findByTransactionId(UUID transactionId) {
        return jpaRepository.findByTransactionId(transactionId);
    }
}