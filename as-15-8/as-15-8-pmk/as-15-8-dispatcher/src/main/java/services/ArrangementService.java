package services;

import arrangement.ArrangementToExecution;
import checkUnits.CheckUnit;
import enums.CheckUnitJobResult;
import events.producers.ArrangementStopEventProducer;
import exceptions.AS_15_8_DispatcherException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Arrangement;
import model.Result;
import model.enums.ArrangementStatus;
import model.enums.CheckType;
import model.enums.Reason;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import remoteEvents.ArrangementStopEvent;
import repositories.ArrangementRepo;
import repositories.ResultRepo;
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

    @Value("${gateway.url}")
    private String gatewayUrl;

    @Getter
    private final Map<Long, Set<Long>> stoppedArrangements = new ConcurrentHashMap<>();

    private final ArrangementRepo arrangementRepo;
    private final ArrangementStopEventProducer arrangementStopEventProducer;
    private final ResultsKafkaService resultsKafkaService;
    private final ActService actService;
    private final ArrangementRestApi arrangementRestApi;
    private final ResultRepo resultRepo;

    private final String GET_CHECK_UNITS_FROM_PPT = "/ppt/arrangements/checkUnits";

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


    @Async
    //@Scheduled(cron = "0 0 0 * * ?")
    @Scheduled(cron = "0 50 09 * * ?")
    void clearStoppedArrangements() {
        evictCaches();
        stoppedArrangements.clear();
        stopAllRunningArrangementsByDayGone();
    }

    void stopAllRunningArrangementsByDayGone() {
        try {
            log.info("Попытка завершения всех мероприятий по шедулеру на текущий день");
            stopAllRunningArrangements(Reason.MANUAL);
        } catch (Exception ex){
            log.error("Ошибка при остановке мероприятий по окончанию для ", ex);
        }
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
    public void stopExecution(Long arrangementId, Long version, Reason reason) {
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
        arrangement.setReason(reason);
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
            else {
                switch (arr.getReason()) {
                    case STOPPED_BY_SERVICE_MODE:
                        arr.setStatus(ArrangementStatus.STOPPED_BY_SERVICE_MODE);
                        break;
                    case STOPPED_BY_MAX_CHECK_UNITS_COUNT:
                        arr.setStatus(ArrangementStatus.STOPPED_BY_MAX_CHECK_UNITS);
                        break;
                    case MANUAL:
                        arr.setStatus(ArrangementStatus.STOPPED);
                        break;
                }
            }
            arrangementRepo.save(arr);
            if (!isStopped && isActAvailable) {
                return actService.createAct(arrangementId);
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

    public void stopAllRunningArrangements(Reason reason) {
        List<Arrangement> arrangements = arrangementRepo.findAllRunning();
        if (!arrangements.isEmpty()) {
            arrangements.forEach(arrangement -> stopExecution(arrangement.getId(), arrangement.getVersion(), reason));
        }
    }

    public ResponseEntity<Object> createManualArrangement(Long arrangementId) {
        try {
            List<CheckUnit> checkUnits = getCheckUnitsFromPPT(arrangementId);
            Arrangement arrangement = createNewManualArrangement(arrangementId, checkUnits.size());
            arrangementRepo.save(arrangement);

            List<Result> results = createResultsForManualArrangement(arrangement, checkUnits);
            saveResults(results);

            return new ResponseEntity<>( HttpStatus.OK);
        } catch (Exception e){
            return new ResponseEntity<>("Ошибка записи ручного мероприятия в репозиторий", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private List<Result> createResultsForManualArrangement(Arrangement arrangement, List<CheckUnit> checkUnits) {
        List<Result> results = new ArrayList<>();
        checkUnits.forEach(checkUnit -> {
            Result result = new Result();
            result.setResult(CheckUnitJobResult.PLANNED);
            result.setId(checkUnit.getContentId());
            result.setArrangement(arrangement);
            result.setCheckType(CheckType.MANUAL);
            result.setCheckUnitType(checkUnit.getType());
            result.setCheckUnitValue(checkUnit.getValue());
            result.setErdiId(checkUnit.getContentId());
            results.add(result);
        });

        return results;
    }

    private void saveResults(List<Result> manualArrResults) {
        manualArrResults.forEach(resultRepo::save);
    }

    private Arrangement createNewManualArrangement(Long arrangementId, int checkUnitsCount) {
        Arrangement arrangement = new Arrangement();
        arrangement.setId(arrangementId);
        arrangement.setCreationDate(LocalDateTime.now());
        arrangement.setStatus(ArrangementStatus.RUNNING);
        arrangement.setIsManual(true);
        arrangement.setVersion(-1L);
        arrangement.setCheckUnitsCount((long) checkUnitsCount);
        arrangement.setMaxCheckUnitsCount(0L);
        return arrangement;
    }

    private List<CheckUnit> getCheckUnitsFromPPT(Long arrangementId) {
        String uri = UriComponentsBuilder.fromUriString(GET_CHECK_UNITS_FROM_PPT)
                .queryParam("id", arrangementId)
                .build()
                .toString();
        try {
            log.info("Получение чек-юнитов мероприятия {} по запросу: {}", arrangementId, uri);

            return WebClient.create(gatewayUrl)
                    .get()
                    .uri(uri)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .exchange()
                    .flatMapMany(clientResponse -> {
                        if(clientResponse.statusCode().equals(HttpStatus.OK)){
                            log.info("Check units мероприятия {} успешно сформированы", arrangementId);
                            return clientResponse.bodyToFlux(new ParameterizedTypeReference<List<CheckUnit>>(){})
                                    .flatMap(Flux::fromIterable);
                        } else {
                            log.warn("Ошибка получения чек-юнитов мероприятия {} в ППМ код возврата {}", arrangementId, clientResponse.statusCode().toString());
                            return (Flux.empty());
                        }
                    })
                    .collectList()
                    .block();

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw AS_15_8_DispatcherException.logAndGet(log, String.format("Ошибка получения чек-юнитов мероприятия %d в ППМ, код возврата %s", arrangementId, ex.getStatusCode()), ex);
        } catch (Exception ex){
            throw AS_15_8_DispatcherException.logAndGet(log, String.format("Ошибка получения чек-юнитов мероприятия %d в ППМ", arrangementId), ex);
        }
    }
}
