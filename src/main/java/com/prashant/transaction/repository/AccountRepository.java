package com.prashant.transaction.repository;

import com.prashant.transaction.model.Account;

import java.util.Optional;

public interface AccountRepository {
    Optional<Account> findByIdForUpdate(Long id);
    Optional<Account> findById(Long id);
    Account save(Account account);
    void flush();
}
