package com.prashant.transaction.repository;

import com.prashant.transaction.model.ProcessedEvent;

public interface ProcessedEventRepository {
    boolean doesEventExist(String eventId) ;
    void save(ProcessedEvent event);
}
