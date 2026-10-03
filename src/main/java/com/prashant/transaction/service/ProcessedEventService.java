package com.prashant.transaction.service;

import com.prashant.transaction.model.ProcessedEvent;
import com.prashant.transaction.repository.ProcessedEventRepository;
import org.springframework.stereotype.Service;

@Service
public class ProcessedEventService {
    private final ProcessedEventRepository processedEventRepository;
    public ProcessedEventService(ProcessedEventRepository processedEventRepository) {
        this.processedEventRepository = processedEventRepository;
    }
    public boolean alreadyProcessed(String eventId) {
        return processedEventRepository.doesEventExist(eventId);
    }
    public void markAsProcessed(String eventId) {
        ProcessedEvent event = new ProcessedEvent();
        event.setEventId(eventId);
        event.setProcessedAt(java.time.LocalDateTime.now());
        processedEventRepository.save(event);
    }
}
