package com.prashant.transaction.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.prashant.transaction.model.OutboxEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class KafkaPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public KafkaPublisher(KafkaTemplate<String, String> kafkaTemplate,
                          ObjectMapper objectMapper,
                          @Value("${spring.kafka.topic}") String topic) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    public boolean publish(OutboxEvent event) {
        try {
            String key = event.getAggregateId();
            String value = event.getPayload();
            kafkaTemplate.send(topic, key, value).get(); // synchronous send for reliability
            return true;
        } catch (Exception e) {
            // Kafka send failed
            e.printStackTrace();
            return false;
        }
    }
}
