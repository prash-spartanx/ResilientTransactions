package com.prashant.transaction.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    private String id;

    @Column(nullable = false, updatable = false)
    private String transactionId; // link to payment/transaction

    @Column(nullable = false)
    private String aggregateType; // e.g. "PAYMENT"

    @Column(nullable = false)
    private String aggregateId;   // e.g. account id

    @Column(nullable = false)
    private String type;          // event type

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private String status = "PENDING";

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime publishedAt;

    // getters and setters
}
