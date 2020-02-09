package services;

import arrangement.ArrangementToExecution;
import events.producers.ArrangementStopEventProducer;
import exceptions.AS_15_8_DispatcherException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Arrangement;
import model.enums.ArrangementStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import remoteEvents.ArrangementStopEvent;
import repositories.ArrangementRepo;
import restapi.ArrangementRestApi;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ArrangementService {

    @Getter
    private Map<Long, Set<Long>> stoppedArrangements = new ConcurrentHashMap<>();

    private final ArrangementRepo arrangementRepo;
    private final ArrangementStopEventProducer arrangementStopEventProducer;
    private final ResultsKafkaService resultsKafkaService;
    private final ActService actService;
    private final ArrangementRestApi arrangementRestApi;

    @PostConstruct
    private void fillStoppedArrangements() {
        stoppedArrangements.putAll(
            arrangementRepo
                .findStopped(LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT))
                .stream()
                .collect(Collectors.toMap(
                        Arrangement::getId,
                        arr -> new HashSet<>(Collections.singletonList(arr.getVersion()))
                ))
        );
    }

    @Scheduled(cron = "0 0 0 * * ?")
    void clearStoppedArrangements() {
        evictCaches();
        stoppedArrangements.clear();
    }

    @CacheEvict(value = "maxCheckUnitsCount", allEntries = true)
    public void evictCaches() {}

    public void createOrRestart(ArrangementToExecution arrangementToExecution) {
        Arrangement arrangement = arrangementRepo
                .findById(arrangementToExecution.getId())
                .orElseGet(Arrangement::new);
        if (arrangement.getId() == null)
            arrangement.setId(arrangementToExecution.getId());
        arrangement.setCheckUnitsCount(arrangementToExecution.getCheckUnitsCount());
        arrangement.setCreationDate(LocalDateTime.now());
        arrangement.setVersion(Optional.ofNullable(arrangementToExecution.getVersion()).orElse(0L));
        arrangement.setStatus(ArrangementStatus.RUNNING);
        arrangement.setMaxCheckUnitsCount(arrangementRestApi.getInterruptViolationNumberFromPPT(arrangementToExecution.getId()));
        arrangementRepo.save(arrangement);

        if(arrangement.getId() != null && arrangement.getStatus().equals(ArrangementStatus.STOPPED)) {
            Optional.ofNullable(stoppedArrangements.get(arrangementToExecution.getId()))
                    .ifPresent(stoppedArr -> {
                        stoppedArr.remove(arrangementToExecution.getVersion());
                        if (stoppedArr.size() == 0)
                            stoppedArrangements.remove(arrangementToExecution.getId());
                    });
        }
    }

    @Transactional
    public void stopExecution(Long arrangementId, Long version) {
        Arrangement arrangement = arrangementRepo
                .findById(arrangementId)
                .orElseThrow(() -> new AS_15_8_DispatcherException("Ошибка остановки мероприятия. Мероприятие не найдено по ID: " + arrangementId));
        if(!arrangement.getVersion().equals(version)) {
            log.warn("Ошибка остановки мероприятия. Мероприятие " + arrangementId + ", версия " + version + " было запущено с новой версией: " + arrangement.getVersion());
            return;
        }
        if(resultsKafkaService.getResultsCount(arrangementId) >= arrangement.getCheckUnitsCount()) {
            log.warn("Ошибка остановки мероприятия. Мероприятие уже выполнено: " + arrangementId + ", " + version);
            return;
        }
        if(stoppedArrangements.containsKey(arrangementId))
            stoppedArrangements.get(arrangementId).add(version);
        else
            stoppedArrangements.put(arrangementId, new HashSet<>(Collections.singletonList(version)));

        arrangement.setStatus(ArrangementStatus.STOPPING);
        arrangementRepo.save(arrangement);

        final ArrangementStopEvent event = new ArrangementStopEvent(arrangementId, version);
        arrangementStopEventProducer.send(event);
    }

    public synchronized boolean isArrangementRunning(Long arrangementId, Long version) {
        return Optional.ofNullable(stoppedArrangements.get(arrangementId))
                .map(stArr -> !stArr.contains(version))
                .orElse(true);
    }

    boolean finishArrangement(Long arrangementId, boolean isStopped, boolean isActAvailable) {
        try {
            Arrangement arr = arrangementRepo.findById(arrangementId)
                    .orElseThrow(() -> new AS_15_8_DispatcherException("Ошибка при закрытии мероприятия. Мероприятие не найдено по ID: " + arrangementId));
            if (!isStopped)
                arr.setStatus(ArrangementStatus.FINISHED);
            else{
                arr.setStatus(ArrangementStatus.STOPPED);
                //arr.setCheckUnitsCount(resultsKafkaService.getResultsCount(arrangementId));
            }
            arrangementRepo.save(arr);
            if (!isStopped && isActAvailable) {
                actService.createAct(arrangementId);
            }
            return true;
        } catch (Exception ex) {
            log.error("Ошибка при завершении мероприятия", ex);
            return false;
        }
    }

    @Cacheable(value = "maxCheckUnitsCount")
    public Long getMaxCheckUnitsCount(Long arrangementId) {
        return arrangementRepo.findMaxCheckUnitsCount(arrangementId).orElse(null);
    }

    public void stopAllRunningArrangements() {
        List<Arrangement> arrangements = arrangementRepo.findAllRunning();
        if (!arrangements.isEmpty()) {
            arrangements.forEach(arrangement -> {
                stopExecution(arrangement.getId(), arrangement.getVersion());
            });
        }
    }
}
