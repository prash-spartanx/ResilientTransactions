package com.prashant.transaction;

import com.prashant.transaction.model.Account;
import com.prashant.transaction.model.OutboxEvent;
import com.prashant.transaction.repository.AccountRepository;
import com.prashant.transaction.repository.OutboxEventRepository;
import com.prashant.transaction.service.OutboxPoller;
import com.prashant.transaction.usecase.TransferUseCase;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class Day4OutboxTest {

    @Autowired
    private TransferUseCase transferUseCase;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private OutboxPoller outboxPoller;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Long sourceAccountId;
    private Long destinationAccountId;

    @BeforeEach
    void setUp() {

        transactionTemplate.executeWithoutResult(status -> {

            Account source = new Account();
            source.setOwnerName("Day4-Source");
            source.setCachedBalance(new BigDecimal("1000.00"));

            source = accountRepository.save(source);
            sourceAccountId = source.getId();

            Account destination = new Account();
            destination.setOwnerName("Day4-Destination");
            destination.setCachedBalance(new BigDecimal("0.00"));

            destination = accountRepository.save(destination);
            destinationAccountId = destination.getId();
        });

        System.out.println("\n==============================================");
        System.out.println("DAY 4 OUTBOX TEST");
        System.out.println("Source      : " + sourceAccountId);
        System.out.println("Destination : " + destinationAccountId);
        System.out.println("==============================================\n");
    }

    @Test
    @DisplayName("Outbox event is published and marked PUBLISHED")
    void shouldPublishOutboxEvent() throws Exception {

        String idempotencyKey = "day4-" + System.currentTimeMillis();

        String transactionId =
                transferUseCase.transferOptimisticWithProtection(
                        sourceAccountId,
                        destinationAccountId,
                        100.0,
                        idempotencyKey
                );

        System.out.println("Transfer transactionId = " + transactionId);

        /*
         * The transfer transaction has completed.
         * The outbox event must therefore exist durably.
         */
        List<OutboxEvent> events =
                outboxEventRepository.findUnpublishedEvents();

        assertThat(events).isNotEmpty();

        OutboxEvent event = events.stream()
                .filter(e -> transactionId.equals(e.getTransactionId()))
                .findFirst()
                .orElseThrow();

        assertThat(event.getStatus().toString()).isEqualTo("PENDING");

        System.out.println("Outbox event created:");
        System.out.println("Event ID       : " + event.getId());
        System.out.println("Transaction ID : " + event.getTransactionId());
        System.out.println("Status         : " + event.getStatus());

        /*
         * Simulate the poller recovering the durable
         * outbox event after the original transaction.
         */
        outboxPoller.pollAndPublishEvents();

        /*
         * The event should no longer appear among
         * unpublished events.
         */
        List<OutboxEvent> remaining =
                outboxEventRepository.findUnpublishedEvents();

        boolean stillUnpublished = remaining.stream()
                .anyMatch(e -> e.getId().equals(event.getId()));

        assertThat(stillUnpublished).isFalse();

        System.out.println("\n==============================================");
        System.out.println("DAY 4 OUTBOX TEST PASSED");
        System.out.println("Event recovered and published successfully.");
        System.out.println("Event ID : " + event.getId());
        System.out.println("==============================================\n");
    }
}