package com.prashant.transaction.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "idempotency_records")
public class IdempotencyRecord {

    @Id
    @Column(nullable = false, unique = true)
    private String idempotencyKey; // Provided by client

    @Column(nullable = false)
    private String requestHash; // Generated server-side from request data

    @Lob
    private String resultPayload; // Serialized response (success/failure)

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

}
