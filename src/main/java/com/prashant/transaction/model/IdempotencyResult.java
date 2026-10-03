package com.prashant.transaction.model;

public enum IdempotencyResult {
      PROCEED,
    REPLAY,
    IN_PROGRESS,
    CONFLICT

}
