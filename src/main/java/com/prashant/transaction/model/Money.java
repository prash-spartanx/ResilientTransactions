package com.prashant.transaction.model;
import lombok.val;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Immutable Money Value Object preventing floating-point rounding errors.
 */
public record Money(BigDecimal amount) {

    public static final int SCALE = 4;
    public static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_EVEN;

    public Money {
        Objects.requireNonNull(amount, "Amount cannot be null");
        amount = amount.setScale(SCALE, ROUNDING_MODE);
    }

    public static Money of(String amount) {
        return new Money(new BigDecimal(amount));
    }

    public static Money of(double amount) {
        return new Money(BigDecimal.valueOf(amount));
    }
    // return the amount as a BigDecimal
    public BigDecimal amount() {
        return amount;
    }

    public Money add(Money other) {
        return new Money(this.amount.add(other.amount));
    }

    public Money subtract(Money other) {
        return new Money(this.amount.subtract(other.amount));
    }

    public boolean isLessThan(Money other) {
        return this.amount.compareTo(other.amount) < 0;
    }

}