package com.prashant.transaction;

import com.prashant.transaction.model.Account;
import com.prashant.transaction.repository.AccountRepository;
import com.prashant.transaction.repository.IdempotencyRepository;
import com.prashant.transaction.usecase.TransferUseCase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class TransferUseCaseDay3Test2 {

    @Autowired
    private TransferUseCase transferUseCase;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private IdempotencyRepository idempotencyRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Long sourceAccountId;
    private Long destinationAccountId;

    @BeforeEach
    void setUp() {

        transactionTemplate.executeWithoutResult(status -> {

            Account source = new Account();
            source.setOwnerName("Prashant");
            source.setCachedBalance(new BigDecimal("1000.00"));
            source = accountRepository.save(source);
            sourceAccountId = source.getId();

            Account destination = new Account();
            destination.setOwnerName("Bob");
            destination.setCachedBalance(new BigDecimal("0.00"));
            destination = accountRepository.save(destination);
            destinationAccountId = destination.getId();
        });

        System.out.println("\n=================================================");
        System.out.println("DAY 3 TEST SETUP");
        System.out.println("Source Account      : " + sourceAccountId);
        System.out.println("Destination Account : " + destinationAccountId);
        System.out.println("=================================================\n");
    }


    @Test
    @DisplayName("20 concurrent requests with different idempotency keys")
    void shouldProcessSameIdempotencyKeyOnlyOnce() throws Exception {

        int threadCount = 20;
        double amount = 100.0;

        ExecutorService executor =
                Executors.newFixedThreadPool(threadCount);

        CountDownLatch startLatch =
                new CountDownLatch(1);

        CountDownLatch finishLatch =
                new CountDownLatch(threadCount);

        AtomicInteger successCount =
                new AtomicInteger();

        AtomicInteger failureCount =
                new AtomicInteger();

        List<String> results =
                Collections.synchronizedList(new ArrayList<>());

        long testStart = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {

            final int threadNumber = i + 1;

            executor.submit(() -> {

                try {

                    startLatch.await();

                    long start =
                            System.currentTimeMillis();

                    String threadIdempotencyKey = UUID.randomUUID().toString();

                    String result =
                            transferUseCase.transferOptimisticWithProtection(
                                    sourceAccountId,
                                    destinationAccountId,
                                    amount,
                                    threadIdempotencyKey
                            );

                    long duration =
                            System.currentTimeMillis() - start;

                    successCount.incrementAndGet();
                    results.add(result);

                    System.out.printf(
                            "[Thread-%02d] SUCCESS | %d ms | transactionId=%s%n",
                            threadNumber,
                            duration,
                            result
                    );

                } catch (Exception e) {

                    failureCount.incrementAndGet();

                    System.out.printf(
                            "[Thread-%02d] REJECTED | %s | %s%n",
                            threadNumber,
                            e.getClass().getSimpleName(),
                            e.getMessage()
                    );

                } finally {

                    finishLatch.countDown();
                }
            });
        }

        System.out.println(
                "Starting " + threadCount +
                        " concurrent requests with DIFFERENT idempotency keys..."
        );

        startLatch.countDown();

        assertThat(finishLatch.await(30, TimeUnit.SECONDS))
                .isTrue();

        executor.shutdown();
        assertThat(successCount.get()).isEqualTo(10);
        assertThat(failureCount.get()).isEqualTo(10);
        long totalTime =
                System.currentTimeMillis() - testStart;


        // --------------------------------------------------
        // RESULT REPORT
        // --------------------------------------------------

        System.out.println("\n================ DAY 3 REPORT ================");
        System.out.println("Total Requests  : " + threadCount);
        System.out.println("Successes       : " + successCount.get());
        System.out.println("Rejected        : " + failureCount.get());
        System.out.println("Total Time      : " + totalTime + " ms");

        if (!results.isEmpty()) {
            System.out.println(
                    "Original Transaction ID : " + results.get(0)
            );
        }


        // --------------------------------------------------
        // VERIFY DATABASE STATE
        // --------------------------------------------------

        transactionTemplate.executeWithoutResult(status -> {

            Account source =
                    accountRepository
                            .findById(sourceAccountId)
                            .orElseThrow();

            Account destination =
                    accountRepository
                            .findById(destinationAccountId)
                            .orElseThrow();
            assertThat(source.getCachedBalance()).isEqualByComparingTo("0.00");
                assertThat(destination.getCachedBalance()).isEqualByComparingTo("1000.00");
                assertThat(source.getCachedBalance().add(destination.getCachedBalance()))
                        .isEqualByComparingTo("1000.00");

                // Both entity versions should increment by 10
                assertThat(source.getVersion()).isEqualTo(10L);
                assertThat(destination.getVersion()).isEqualTo(10L);

            System.out.println("\n--------------- FINAL DATABASE STATE ---------------");
            System.out.println(
                    "Source Balance      : " +
                            source.getCachedBalance()
            );

            System.out.println(
                    "Destination Balance : " +
                            destination.getCachedBalance()
            );

            System.out.println(
                    "Total Money         : " +
                            source.getCachedBalance()
                                    .add(destination.getCachedBalance())
            );

            System.out.println(
                    "Source Version      : " +
                            source.getVersion()
            );

            System.out.println(
                    "Destination Version : " +
                            destination.getVersion()
            );

            System.out.println("====================================================\n");





           });

        System.out.println("=================================================\n");
    }
}