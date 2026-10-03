package com.prashant.transaction.repository;

import com.prashant.transaction.model.LedgerEntry;

import java.util.List;
import java.util.UUID;

public interface LedgerEntryRepository {
    LedgerEntry save(LedgerEntry entry);
    List<LedgerEntry> findByTransactionId(UUID transactionId);
}
