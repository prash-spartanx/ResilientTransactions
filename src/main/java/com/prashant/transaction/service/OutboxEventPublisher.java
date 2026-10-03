package com.prashant.transaction.service;

import com.prashant.transaction.model.OutboxEvent;
import com.prashant.transaction.repository.OutboxEventRepository;
import org.springframework.stereotype.Service;

@Service
public class OutboxEventPublisher implements EventPublisher {

    private final OutboxEventRepository repository;

    public OutboxEventPublisher(OutboxEventRepository repository) {
        this.repository = repository;
    }

    @Override
    public void publish(String eventId ,String transactionId, String aggregateType, String aggregateId, String type, String payload) {
        OutboxEvent event = new OutboxEvent();
        event.setId(eventId);
        event.setTransactionId(transactionId);
        event.setAggregateType(aggregateType);
        event.setAggregateId(aggregateId);
        event.setType(type);
        event.setPayload(payload);
        repository.save(event);
    }
}