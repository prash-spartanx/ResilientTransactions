package com.prashant.transaction.service;

import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PaymentEventProcessor {

    private final Map<String, AtomicInteger> attempts = new ConcurrentHashMap<>();

    @Retry(name = "paymentNotification")
    public void notifyPaymentCompleted(String eventId) {

        int attempt = attempts
                .computeIfAbsent(eventId, key -> new AtomicInteger(0))
                .incrementAndGet();

        System.out.println(
                "Processing payment notification | eventId=" + eventId +
                        " | attempt=" + attempt
        );

        // TEMPORARY TEST FAILURE
        if (attempt < 3) {
            throw new RuntimeException(
                    "Simulated notification service failure"
            );
        }

        System.out.println(
                "PAYMENT COMPLETED — Notification sent for event: " + eventId
        );

        attempts.remove(eventId);
    }
}