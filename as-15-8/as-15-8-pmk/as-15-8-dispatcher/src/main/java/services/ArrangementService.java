package services;

import arrangement.ArrangementToExecution;
import exceptions.AS_15_8_DispatcherException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import model.Arrangement;
import model.enums.ArrangementStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import remoteEvents.ArrangementStopEvent;
import repositories.ArrangementRepo;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ArrangementService {

    @Value("${stop.arrangements.destination.service}")
    private String stopArrangementsDestinationService;

    @Getter
    private Map<Long, Set<Long>> stoppedArrangements = new ConcurrentHashMap<>();

    private final ApplicationContext context;
    private final ArrangementRepo arrangementRepo;
    private final ResultsKafkaService resultsKafkaService;

    @PostConstruct
    private void fillStoppedArrangements() {
        stoppedArrangements.putAll(
            arrangementRepo
                .findStopped(LocalDateTime.now())
                .stream()
                .collect(Collectors.toMap(
                        Arrangement::getId,
                        arr -> new HashSet<>(Collections.singletonList(arr.getVersion()))
                ))
        );
    }

    @Scheduled(cron = "0 0 0 * * ?")
    void clearStoppedArrangements() {
        stoppedArrangements.clear();
    }

    public void createOrRestart(ArrangementToExecution arrangementToExecution) {
        Arrangement arrangement = arrangementRepo
                .findById(arrangementToExecution.getId())
                .orElseGet(Arrangement::new);
        if(arrangement.getId() == null) {
            arrangement.setId(arrangementToExecution.getId());
            arrangement.setCheckUnitsCount(arrangementToExecution.getCheckUnitsCount());
        } else {
            arrangement.setCheckUnitsCount(arrangement.getCheckUnitsCount() + arrangementToExecution.getCheckUnitsCount());
            Optional.ofNullable(stoppedArrangements.get(arrangementToExecution.getId()))
                    .ifPresent(stoppedArr -> {
                        stoppedArr.remove(arrangementToExecution.getVersion());
                        if (stoppedArr.size() == 0)
                            stoppedArrangements.remove(arrangementToExecution.getId());
                    });
        }
        arrangement.setCreationDate(LocalDateTime.now());
        arrangement.setVersion(Optional.ofNullable(arrangementToExecution.getVersion()).orElse(0L));
        arrangement.setStatus(ArrangementStatus.RUNNING);
        arrangementRepo.save(arrangement);
    }

    @Transactional
    public void stopExecution(Long arrangementId, Long version) {
        Arrangement arrangement = arrangementRepo
                .findByIdAndVersion(arrangementId, version)
                .orElseThrow(() -> new AS_15_8_DispatcherException("Ошибка остановки мероприятия. Мероприятие не найдено в БД по id " + arrangementId + " и версии " + version));
        arrangement.setStatus(ArrangementStatus.STOPPING);
        arrangement.setCheckUnitsCount(resultsKafkaService.getResultsCount(arrangementId));
        arrangementRepo.save(arrangement);

        if(stoppedArrangements.containsKey(arrangementId))
            stoppedArrangements.get(arrangementId).add(version);
        else
            stoppedArrangements.put(arrangementId, new HashSet<>(Collections.singletonList(version)));

        final ArrangementStopEvent event = new ArrangementStopEvent(this, context.getId(), stopArrangementsDestinationService, arrangementId, version);
        context.publishEvent(event);
    }

    public synchronized boolean isArrangementRunning(Long arrangementId, Long version) {
        return Optional.ofNullable(stoppedArrangements.get(arrangementId))
                .map(stArr -> !stArr.contains(version))
                .orElse(true);
    }
}
