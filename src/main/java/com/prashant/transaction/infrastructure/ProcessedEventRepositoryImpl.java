package com.prashant.transaction.infrastructure;

import com.prashant.transaction.model.ProcessedEvent;
import com.prashant.transaction.repository.ProcessedEventRepository;
import org.springframework.stereotype.Component;

@Component
public class ProcessedEventRepositoryImpl implements ProcessedEventRepository {
    private final SpringDataProcessedEventRepository springDataProcessedEventRepository;
    public ProcessedEventRepositoryImpl(SpringDataProcessedEventRepository springDataProcessedEventRepository) {
        this.springDataProcessedEventRepository = springDataProcessedEventRepository;
    }
    @Override
    public  boolean doesEventExist(String eventId) {
        return springDataProcessedEventRepository.existsById(eventId);
    }
    @Override
    public void save(ProcessedEvent event ) {
        springDataProcessedEventRepository.save(event);
    }

}
