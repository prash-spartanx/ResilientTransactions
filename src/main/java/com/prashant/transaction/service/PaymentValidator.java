package com.prashant.transaction.service;

import com.prashant.transaction.repository.AccountRepository;
import org.springframework.stereotype.Component;

@Component
public class PaymentValidator {
    private final AccountRepository accountRepository;
    public PaymentValidator(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
    public boolean isValid(long sourceAccountId, long destinationAccountId, double amount) {
        if (sourceAccountId == destinationAccountId) {
            return false;
        }
        if (amount <= 0) {
            return false;
        }
        if (!accountRepository.findById(sourceAccountId).isPresent()) {
            return false;
        }
        if (!accountRepository.findById(destinationAccountId).isPresent()) {
            return false;
        }
        return true;
    }
    // isvalidPessimistic method
    public boolean isValidPessimistic(long sourceAccountId, long destinationAccountId, double amount) {
        if (sourceAccountId == destinationAccountId) {
            return false;
        }
        if (amount <= 0) {
            return false;
        }
        if (!accountRepository.findByIdForUpdate(sourceAccountId).isPresent()) {
            return false;
        }
        if (!accountRepository.findByIdForUpdate(destinationAccountId).isPresent()) {
            return false;
        }
        return true;

    }
    // optimistic locking version
    public boolean isValidOptimistic(long sourceAccountId, long destinationAccountId, double amount) {
        if (sourceAccountId == destinationAccountId) {
            return false;
        }
        if (amount <= 0) {
            return false;
        }
        if (!accountRepository.findById(sourceAccountId).isPresent()) {
            return false;
        }
        if (!accountRepository.findById(destinationAccountId).isPresent()) {
            return false;
        }
        return true;
    }
}
