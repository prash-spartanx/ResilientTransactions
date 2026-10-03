package com.prashant.transaction.infrastructure;

import com.prashant.transaction.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SpringDataOutboxEventRepository extends JpaRepository<OutboxEvent, String> {
    @Query(value = "SELECT * FROM outbox_events WHERE status = 'PENDING' ORDER BY created_at FOR UPDATE",
            nativeQuery = true)
    List<OutboxEvent> findUnpublishedEvents();
}
