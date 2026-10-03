package  com.prashant.transaction.infrastructure;
import com.prashant.transaction.infrastructure.SpringDataOutboxEventRepository;
import com.prashant.transaction.model.OutboxEvent;
import com.prashant.transaction.repository.OutboxEventRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OutboxEventRepositoryImpl implements OutboxEventRepository {

    private final SpringDataOutboxEventRepository springRepo;

    public OutboxEventRepositoryImpl(SpringDataOutboxEventRepository springRepo) {
        this.springRepo = springRepo;
    }

    @Override
    public void save(OutboxEvent event) {
        springRepo.save(event);
    }

    @Override
    public List<OutboxEvent> findUnpublishedEvents() {
        return springRepo.findUnpublishedEvents();
    }

    @Override
    public boolean markEventAsPublished(String eventId) {
        return springRepo.findById(eventId).map(event -> {
            event.setStatus("PUBLISHED");
            event.setPublishedAt(java.time.LocalDateTime.now());
            springRepo.save(event);
            return true;
        }).orElse(false);
    }
}