package com.prashant.transaction.service;

public class InMemoryLockProvider implements LockProvider{
    // Implement a simple in-memory lock provider using a ConcurrentHashMap
    private final java.util.concurrent.ConcurrentHashMap<String, Object> locks ;
    public InMemoryLockProvider() {
        this.locks = new java.util.concurrent.ConcurrentHashMap<>();
    }
    @Override
    public boolean acquireLock(String key) {
        return locks.putIfAbsent(key, new Object()) == null;
    }
    @Override
    public boolean releaseLock(String key) {
        return locks.remove(key) != null;
    }
}
