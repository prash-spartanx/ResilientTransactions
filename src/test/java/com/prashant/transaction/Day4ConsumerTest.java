package com.prashant.transaction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prashant.transaction.model.OutboxEvent;
import com.prashant.transaction.repository.OutboxEventRepository;
import com.prashant.transaction.repository.ProcessedEventRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class Day4ConsumerTest {

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private ProcessedEventRepository processedEventRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldConsumeAndRecordProcessedEvent() throws Exception {

        System.out.println();
        System.out.println("=================================================");
        System.out.println("DAY 4 CONSUMER TEST");
        System.out.println("=================================================");

        // Create 2 fresh PENDING outbox events
        List<String> eventIds = new ArrayList<>();

        for (int i = 1; i <= 2; i++) {

            OutboxEvent event = new OutboxEvent();

            String eventId = UUID.randomUUID().toString();
            String transactionId = UUID.randomUUID().toString();

            event.setId(eventId);
            event.setTransactionId(transactionId);
            event.setAggregateType("PAYMENT");
            event.setAggregateId(UUID.randomUUID().toString());
            event.setType("PAYMENT_SETTLED");

            event.setPayload("""
                    {
                      "eventId": "%s",
                      "transactionId": "%s",
                      "sourceAccountId": 9,
                      "destinationAccountId": 10,
                      "amount": 100.00,
                      "eventType": "PAYMENT_SETTLED",
                      "createdAt": "%s"
                    }
                    """.formatted(
                    eventId,
                    transactionId,
                    LocalDateTime.now()
            ));

            event.setStatus("PENDING");
            event.setCreatedAt(LocalDateTime.now());
            event.setPublishedAt(null);

            outboxEventRepository.save(event);

            eventIds.add(eventId);

            System.out.println(
                    "Created outbox event " + i + " : " + eventId
            );
        }

        System.out.println("2 fresh PENDING outbox events created.");

        /*
         * Do NOT manually call KafkaPublisher.
         *
         * The scheduled OutboxPoller will automatically find
         * these PENDING events and publish them to Kafka.
         */

        System.out.println("Waiting for OutboxPoller and consumer...");

        long timeout = System.currentTimeMillis() + 15000;

        boolean allProcessed = false;

        while (System.currentTimeMillis() < timeout) {

            allProcessed = true;

            for (String eventId : eventIds) {

                if (!processedEventRepository.doesEventExist(eventId)) {
                    allProcessed = false;
                    break;
                }
            }

            if (allProcessed) {
                break;
            }

            Thread.sleep(200);
        }

        assertTrue(
                allProcessed,
                "Consumer should process both events and create processed_events records"
        );

        System.out.println();
        System.out.println("Both events were processed successfully.");

        for (String eventId : eventIds) {
            assertTrue(
                    processedEventRepository.doesEventExist(eventId),
                    "processed_events should contain event: " + eventId
            );

            System.out.println(
                    "Processed event confirmed: " + eventId
            );
        }

        System.out.println();
        System.out.println("=================================================");
        System.out.println("DAY 4 CONSUMER TEST PASSED");
        System.out.println("Both events were consumed successfully.");
        System.out.println("processed_events records confirmed.");
        System.out.println("=================================================");
    }
}