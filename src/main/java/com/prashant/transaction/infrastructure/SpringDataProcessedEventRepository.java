package com.prashant.transaction.infrastructure;

import com.prashant.transaction.model.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpringDataProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {

}
