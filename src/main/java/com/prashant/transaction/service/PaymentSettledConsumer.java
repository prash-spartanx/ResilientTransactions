package com.prashant.transaction.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

@Service
public class PaymentSettledConsumer {

    private final ObjectMapper objectMapper;
    private final ProcessedEventService processedEventService;
    private final PaymentEventProcessor paymentEventProcessor;

    public PaymentSettledConsumer(
            ObjectMapper objectMapper,
            ProcessedEventService processedEventService,
            PaymentEventProcessor paymentEventProcessor) {

        this.objectMapper = objectMapper;
        this.processedEventService = processedEventService;
        this.paymentEventProcessor = paymentEventProcessor;
    }

    @KafkaListener(
            topics = "${spring.kafka.topic}",
            groupId = "payment-audit-service",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(
            ConsumerRecord<String, String> record,
            Acknowledgment ack) {

        try {
            // Parse the Kafka event
            JsonNode json = objectMapper.readTree(record.value());

            String eventId = json.get("eventId").asText();

            // Check whether this event was already processed
            if (processedEventService.alreadyProcessed(eventId)) {

                System.out.println(
                        "Skipping already processed event: " + eventId
                );

                ack.acknowledge();
                return;
            }

            // Process the event.
            // Resilience4j handles the processing retries.
            paymentEventProcessor.notifyPaymentCompleted(eventId);

            // Only mark the event processed after successful processing.
            processedEventService.markAsProcessed(eventId);

            // Acknowledge only after successful processing.
            ack.acknowledge();

        } catch (Exception e) {

            System.err.println(
                    "Error consuming message: " + e.getMessage()
            );

            /*
             * IMPORTANT:
             *
             * Do not acknowledge the message.
             * More importantly, do not swallow the exception.
             *
             * The exception must reach Spring Kafka's
             * DefaultErrorHandler, which will send the
             * failed record to the Dead Letter Topic.
             */
            throw new RuntimeException(
                    "Payment event processing failed",
                    e
            );
        }
    }
}