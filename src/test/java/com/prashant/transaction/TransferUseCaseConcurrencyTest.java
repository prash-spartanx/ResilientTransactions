package com.prashant.transaction;

import com.prashant.transaction.model.Account;
import com.prashant.transaction.repository.AccountRepository;
import com.prashant.transaction.usecase.TransferUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;


@SpringBootTest
class TransferUseCaseConcurrencyTest {

    private static final Logger log = LoggerFactory.getLogger(TransferUseCaseConcurrencyTest.class);

    @Autowired
    private TransferUseCase transferUseCase;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Long sourceAccountId;
    private Long destinationAccountId;

    // Data class to capture individual thread metrics
    private static class ThreadExecutionMetric {
        int threadIndex;
        String threadName;
        long startTimeMs;
        long durationMs;
        boolean success;
        String errorMessage;

        public ThreadExecutionMetric(int threadIndex, String threadName, long startTimeMs, long durationMs, boolean success, String errorMessage) {
            this.threadIndex = threadIndex;
            this.threadName = threadName;
            this.startTimeMs = startTimeMs;
            this.durationMs = durationMs;
            this.success = success;
            this.errorMessage = errorMessage;
        }
    }

    @BeforeEach
    void setUp() {
        transactionTemplate.executeWithoutResult(status -> {
            Account source = new Account();
            source.setOwnerName("Alice");
            source.setCachedBalance(new BigDecimal("1000.00"));
            source = accountRepository.save(source);
            sourceAccountId = source.getId();

            Account destination = new Account();
            destination.setOwnerName("Bob");
            destination.setCachedBalance(new BigDecimal("0.00"));
            destination = accountRepository.save(destination);
            destinationAccountId = destination.getId();
        });
    }

    @Test
    @DisplayName("Test concurrent transfers with detailed per-thread metrics and timing analysis")
    void testConcurrentTransfers() throws InterruptedException {
        int numberOfThreads = 10;
        double transferAmount = 100.0;

        ExecutorService executorService = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(numberOfThreads);

        AtomicInteger successfulTransfers = new AtomicInteger(0);
        AtomicInteger failedTransfers = new AtomicInteger(0);
        List<ThreadExecutionMetric> metrics = Collections.synchronizedList(new ArrayList<>());

        long globalStartTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfThreads; i++) {
            final int threadIndex = i + 1;
            executorService.submit(() -> {
                String threadName = Thread.currentThread().getName();
                long startExecution = 0;
                long duration = 0;
                boolean isSuccess = false;
                String errorMsg = "None";

                try {
                    startLatch.await(); // Synchronized release point
                    startExecution = System.currentTimeMillis() - globalStartTime;
                    String idempotencyKey = "transfer-" + threadIndex + "-" + System.currentTimeMillis();
                    // Execute transfer
                    transferUseCase.transferOptimistic(sourceAccountId, destinationAccountId, transferAmount,idempotencyKey);

                    long endExecution = System.currentTimeMillis() - globalStartTime;
                    duration = endExecution - startExecution;
                    isSuccess = true;
                    successfulTransfers.incrementAndGet();

                    log.info("[Thread-{}] SUCCESS | Queued Wait + Exec Time: {} ms", threadIndex, duration);

                } catch (Exception e) {
                    long endExecution = System.currentTimeMillis() - globalStartTime;
                    duration = endExecution - startExecution;
                    errorMsg = e.getClass().getSimpleName() + " - " + e.getMessage();
                    failedTransfers.incrementAndGet();

                    log.error("[Thread-{}] FAILED | Time before failure: {} ms | Cause: {}", threadIndex, duration, errorMsg);
                } finally {
                    metrics.add(new ThreadExecutionMetric(threadIndex, threadName, startExecution, duration, isSuccess, errorMsg));
                    endLatch.countDown();
                }
            });
        }

        // Trigger simultaneous execution
        startLatch.countDown();

        boolean completed = endLatch.await(15, TimeUnit.SECONDS);
        long globalTotalDuration = System.currentTimeMillis() - globalStartTime;
        executorService.shutdown();

        assertThat(completed).isTrue();

        // Statistical Analysis
        DoubleSummaryStatistics stats = metrics.stream()
                .mapToDouble(m -> m.durationMs)
                .summaryStatistics();

        // Print Structured Performance Report
        System.out.println("\n============ CONCURRENCY METRICS REPORT ============");
        System.out.println(String.format("%-10s | %-15s | %-12s | %-12s | %-10s", "Thread #", "Name", "Start Offset", "Duration", "Status"));
        System.out.println("--------------------------------------------------------------------");
        for (ThreadExecutionMetric m : metrics) {
            System.out.println(String.format("Thread-%-3d | %-15s | %-10d ms | %-10d ms | %-10s",
                    m.threadIndex, m.threadName, m.startTimeMs, m.durationMs, m.success ? "SUCCESS" : "FAILED"));
        }
        System.out.println("=====================================================");
        System.out.println("Total Execution Time (Wall Clock): " + globalTotalDuration + " ms");
        System.out.println("Average Duration per Thread      : " + String.format("%.2f", stats.getAverage()) + " ms");
        System.out.println("Min Duration (First in Lock)     : " + (long) stats.getMin() + " ms");
        System.out.println("Max Duration (Last in Lock Queue) : " + (long) stats.getMax() + " ms");
        System.out.println("Successful Executions            : " + successfulTransfers.get());
        System.out.println("Failed Executions                : " + failedTransfers.get());

        // Validate final database state
        transactionTemplate.executeWithoutResult(status -> {
            Account updatedSource = accountRepository.findById(sourceAccountId).orElseThrow();
            Account updatedDestination = accountRepository.findById(destinationAccountId).orElseThrow();

            System.out.println("-----------------------------------------------------");
            System.out.println("Source Cached Balance : " + updatedSource.getCachedBalance());
            System.out.println("Source Version        : " + updatedSource.getVersion());
            System.out.println("Dest Cached Balance   : " + updatedDestination.getCachedBalance());
            System.out.println("Dest Version          : " + updatedDestination.getVersion());
            System.out.println("Total System Money    : " + updatedSource.getCachedBalance().add(updatedDestination.getCachedBalance()));
            System.out.println("=====================================================\n");
        });
    }
}