package com.prashant.transaction.service;
public interface EventPublisher {
    void publish(String eventId ,String transactionId, String aggregateType, String aggregateId, String type, String payload);
}
