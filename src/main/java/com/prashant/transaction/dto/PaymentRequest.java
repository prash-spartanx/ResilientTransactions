package com.prashant.transaction.dto;

import com.prashant.transaction.model.Money;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaymentRequest {
    String idempotencyKey;
    Long sourceAccountId;
    Long destinationAccountId;
    Money amount;
}
