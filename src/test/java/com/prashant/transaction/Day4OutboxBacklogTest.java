package com.prashant.transaction;

import com.prashant.transaction.model.OutboxEvent;
import com.prashant.transaction.repository.OutboxEventRepository;
import com.prashant.transaction.service.OutboxPoller;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class Day4OutboxBacklogTest {

    @Autowired
    private OutboxEventRepository outboxEventRepository;

    @Autowired
    private OutboxPoller outboxPoller;

    @Test
    void shouldDrainOutboxBacklog() {

        System.out.println();
        System.out.println("=================================================");
        System.out.println("DAY 4 OUTBOX BACKLOG PERFORMANCE TEST");
        System.out.println("=================================================");

        List<OutboxEvent> pendingBefore =
                outboxEventRepository.findUnpublishedEvents();

        int pendingBeforeCount = pendingBefore.size();

        System.out.println("Pending events before : " + pendingBeforeCount);

        if (pendingBeforeCount == 0) {
            System.out.println("No pending outbox events found.");
            System.out.println("Run the Flyway backlog migration first.");
            return;
        }

        long startTime = System.nanoTime();

        outboxPoller.pollAndPublishEvents();

        long endTime = System.nanoTime();

        long elapsedNanos = endTime - startTime;

        List<OutboxEvent> pendingAfter =
                outboxEventRepository.findUnpublishedEvents();

        int pendingAfterCount = pendingAfter.size();

        int publishedCount = pendingBeforeCount - pendingAfterCount;

        double elapsedMillis = elapsedNanos / 1_000_000.0;
        double averageMillisPerEvent =
                elapsedMillis / pendingBeforeCount;

        double eventsPerSecond =
                pendingBeforeCount / (elapsedNanos / 1_000_000_000.0);

        System.out.println();
        System.out.println("=================================================");
        System.out.println("RESULT");
        System.out.println("=================================================");

        System.out.println("Events discovered     : " + pendingBeforeCount);
        System.out.println("Events published      : " + publishedCount);
        System.out.println("Events remaining      : " + pendingAfterCount);

        System.out.printf("Total time            : %.2f ms%n", elapsedMillis);
        System.out.printf("Average/event         : %.2f ms%n",
                averageMillisPerEvent);
        System.out.printf("Throughput            : %.2f events/sec%n",
                eventsPerSecond);

        assertEquals(
                0,
                pendingAfterCount,
                "All pending outbox events should be published"
        );

        assertEquals(
                pendingBeforeCount,
                publishedCount,
                "Every pending outbox event should be published"
        );

        System.out.println();
        System.out.println("=================================================");
        System.out.println("DAY 4 OUTBOX BACKLOG TEST PASSED");
        System.out.println("=================================================");
    }
}