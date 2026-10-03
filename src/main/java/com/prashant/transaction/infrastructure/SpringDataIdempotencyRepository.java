package com.prashant.transaction.infrastructure;

import com.prashant.transaction.model.IdempotencyRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataIdempotencyRepository extends JpaRepository<IdempotencyRecord, String> {

}
