package com.prashant.transaction.repository;

import com.prashant.transaction.model.OutboxEvent;

import java.util.List;
import java.util.Optional;

public interface OutboxEventRepository {
    void save(OutboxEvent event);
    List<OutboxEvent> findUnpublishedEvents();

    boolean markEventAsPublished(String eventId);
}
