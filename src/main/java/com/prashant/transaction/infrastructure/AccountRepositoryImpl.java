package com.prashant.transaction.infrastructure;

import com.prashant.transaction.model.Account;
import com.prashant.transaction.repository.AccountRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AccountRepositoryImpl implements AccountRepository {

    private final SpringDataAccountRepository jpaRepository;

    public AccountRepositoryImpl(SpringDataAccountRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Account> findById(Long id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Account save(Account account) {
        return jpaRepository.save(account);
    }
    @Override
    public Optional<Account> findByIdForUpdate(Long id) {
        return jpaRepository.findByIdForUpdate(id);
    }
    @Override
    public void flush() {
        jpaRepository.flush();
    }
}
