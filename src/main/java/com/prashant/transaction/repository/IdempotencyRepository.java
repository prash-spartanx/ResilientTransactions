package com.prashant.transaction.repository;

import com.prashant.transaction.model.IdempotencyRecord;

public interface IdempotencyRepository {
     IdempotencyRecord findByKey(String idempotencyKey);
     void save(IdempotencyRecord record);
     boolean exists(String idempotencyKey);
}
