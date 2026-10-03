package com.prashant.transaction.infrastructure;

import com.prashant.transaction.model.IdempotencyRecord;
import com.prashant.transaction.repository.IdempotencyRepository;
import org.springframework.stereotype.Component;

@Component
public class IdempotencyRepositoryImpl implements IdempotencyRepository {
    private final  SpringDataIdempotencyRepository springDataIdempotencyRepository;
    public IdempotencyRepositoryImpl(SpringDataIdempotencyRepository springDataIdempotencyRepository) {
        this.springDataIdempotencyRepository = springDataIdempotencyRepository;
    }
    @Override
    public boolean exists(String idempotencyKey) {
        return springDataIdempotencyRepository.existsById(idempotencyKey);
    }
    @Override
    public IdempotencyRecord findByKey(String idempotencyKey) {
        return springDataIdempotencyRepository.findById(idempotencyKey).orElse(null);
    }
    @Override
    public void save(IdempotencyRecord record) {
        springDataIdempotencyRepository.save(record);
    }
}
