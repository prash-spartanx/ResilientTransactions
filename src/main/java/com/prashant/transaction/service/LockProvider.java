package com.prashant.transaction.service;

public interface LockProvider {
    public boolean acquireLock(String key);
    public boolean releaseLock(String key);
}
