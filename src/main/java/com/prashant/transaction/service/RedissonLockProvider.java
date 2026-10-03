package com.prashant.transaction.service;

import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

@Service
public class RedissonLockProvider implements LockProvider {

    private final RedissonClient redissonClient;

    public RedissonLockProvider(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public boolean acquireLock(String key) {
        RLock lock = redissonClient.getLock(key);

        try {
            return lock.tryLock(30, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    @Override
    public boolean releaseLock(String key) {
        RLock lock = redissonClient.getLock(key);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            return true;
        }
        return false;
    }
}
