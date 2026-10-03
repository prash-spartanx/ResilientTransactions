package com.prashant.transaction.service;

import com.prashant.transaction.model.OutboxEvent;
import com.prashant.transaction.repository.OutboxEventRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
@Component
public class OutboxPoller {
    private final OutboxEventRepository outboxRepository;
    private final KafkaPublisher kafkaPublisher;
    public OutboxPoller(OutboxEventRepository outboxRepository
    , KafkaPublisher kafkaPublisher) {
        this.outboxRepository = outboxRepository;
        this.kafkaPublisher = kafkaPublisher;
    }
    @Scheduled(fixedDelay = 5000) // Poll every 5 seconds
    public void pollAndPublishEvents() {
         List<OutboxEvent> events =  outboxRepository.findUnpublishedEvents();
         if(events.isEmpty()) {
             System.out.println("No unpublished events found.");
             return;
         }
         for (OutboxEvent event : events) {
             try {
                 //  publish th event to kafka and if successful mark the event as published
                 boolean published = kafkaPublisher.publish(event);
                 // if got acknmledgement from kafka then mark the event as published
                    if(published) {
                        boolean marked = outboxRepository.markEventAsPublished(event.getId());
                        if(marked) {
                            System.out.println("Successfully published and marked event as published: " + event.getId());
                        } else {
                            System.err.println("Failed to mark event as published: " + event.getId());
                        }
                    } else {
                        System.err.println("Failed to publish event to Kafka: " + event.getId());
                    }
             } catch (Exception e) {
                 System.err.println("Failed to publish event: " + event.getId() + ". Error: " + e.getMessage());
             }

         }
    }

}
