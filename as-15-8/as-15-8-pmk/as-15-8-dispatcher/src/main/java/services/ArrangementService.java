package services;

import arrangement.ArrangementStatusNotification;
import arrangement.ArrangementToExecution;
import checkUnits.CheckUnit;
import enums.ArrangementEvents;
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
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
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
    private Map<Long, Set<Long>> stoppedArrangements = new ConcurrentHashMap<>();

    private final ArrangementRepo arrangementRepo;
    private final ArrangementStopEventProducer arrangementStopEventProducer;
    private final ResultsKafkaService resultsKafkaService;
    private final ActService actService;
    private final ResultRepo resultRepo;

    private final String GET_CHECK_UNITS_FROM_PPT = "/ppt/arrangements/checkUnits";
    private final String PPT_STATUS_ENDPOINT = "/ppt/arrangements/status";
    private final String PPT_IS_ACT_AVAILABLE_FOR_AUTOMATIC_SEND = "/arrangements/act_available_for_automatic_send";
    private final String PPT_ACT_SENT_STATUS = "/arrangements/act_sent_status";
    private final String PPT_ARRANGEMENT_INTERRUPT_VIOLATION_NUMBER = "/arrangements/interrupt_violation_number";

    private final OAuth2RestTemplate restTemplate;

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

    @Scheduled(cron = "0 0/5 0 ? * *")
    public void stopAllRunningArrangementsByDayGone() {
        try {
            stopAllRunningArrangements(Reason.STOPPED_BY_DAY_GONE);
        } catch (Exception ex){
            log.error("Ошибка при остановке мероприятий по кончанию для ", ex);
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
        arrangement.setMaxCheckUnitsCount(getInterruptViolationNumberFromPPT(arrangementToExecution.getId()));
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

        sendStatusNotificationToPPT(new ArrangementStatusNotification(
                arrangement.getId(),
                ArrangementEvents.PREPARE_TO_STOP,
                getCompletionPerscent(arrangement)
        ));
    }

    @Transactional
    public boolean finishArrangement(Long arrangementId) {
        Arrangement arrangement = arrangementRepo
                .findById(arrangementId)
                .orElseThrow(() -> new AS_15_8_DispatcherException("Ошибка завершения мероприятия. Мероприятие не найдено по ID: " + arrangementId));
        arrangement.setStatus(ArrangementStatus.FINISHED);
        arrangementRepo.save(arrangement);

        if (sendStatusNotificationToPPT(new ArrangementStatusNotification(
                arrangement.getId(),
                ArrangementEvents.FINISH,
                getCompletionPerscent(arrangement)))) {
            return true;
        } else return false;
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
                    case STOPPED_BY_DAY_GONE:
                        arr.setStatus(ArrangementStatus.STOPPED_BY_DAY_GONE);
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
            arrangements.forEach(arrangement -> {
                stopExecution(arrangement.getId(), arrangement.getVersion(), reason);
            });
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
        manualArrResults.forEach(result -> {
            resultRepo.save(result);
        });
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


    public boolean sendStopOrFinishedStatusNotificationToPPT(Long arrangementId, boolean isStopped){
        ArrangementStatusNotification notification = createStatusNotification(arrangementId, isStopped);
        return sendStatusNotificationToPPT(notification);
    }

    public boolean sendStatusNotificationToPPT(ArrangementStatusNotification notification){

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        MappingJacksonValue jacksonValue = new MappingJacksonValue(notification);
        HttpEntity<MappingJacksonValue> entity = new HttpEntity<>(jacksonValue, headers);

        log.info("Отправка сообщения с изменением статуса мероприятия {}, путь: " + PPT_STATUS_ENDPOINT, notification.getArrangementId());
        try {
            restTemplate.put(UriComponentsBuilder
                    .fromHttpUrl(gatewayUrl)
                    .path(PPT_STATUS_ENDPOINT)
                    .queryParam("id", notification.getArrangementId())
                    .build().toString(), entity);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.info("Ошибка отправки сообщения с изменением статуса мероприятия, " + notification.getArrangementId() + " путь: " + PPT_STATUS_ENDPOINT + ", код возврата " + ex.getStatusCode());
            return false;
        }
        log.info("Сообщение с изменением статуса мероприятия {} успешно отправлено, путь: " + PPT_STATUS_ENDPOINT, notification.getArrangementId());
        return true;
    }

    private ArrangementStatusNotification createStatusNotification(Long arrangementId, boolean isStopped) {
        ArrangementStatusNotification notification = null;

        Arrangement arr = arrangementRepo.findById(arrangementId).orElseThrow(() ->
                new AS_15_8_DispatcherException("Ошибка создания уведомления для отправки в ППМ или ППТ, мероприятие не найдено id:" + arrangementId));

        switch (arr.getReason()) {
            case MANUAL:
                notification = new ArrangementStatusNotification(
                        arrangementId,
                        isStopped ? ArrangementEvents.STOP : ArrangementEvents.FINISH,
                        getCompletionPerscent(arr));
                break;
            case STOPPED_BY_MAX_CHECK_UNITS_COUNT:
                notification = new ArrangementStatusNotification(arrangementId,
                        ArrangementEvents.STOP_BY_MAX_CHECK_UNITS_COUNT,
                        getCompletionPerscent(arr));
                break;
            case STOPPED_BY_SERVICE_MODE:
                notification = new ArrangementStatusNotification(arrangementId,
                        ArrangementEvents.STOP_BY_SERVICE_MODE,
                        getCompletionPerscent(arr));
                break;
            case STOPPED_BY_DAY_GONE:
                notification = new ArrangementStatusNotification(arrangementId,
                        ArrangementEvents.STOP_BY_DAY_GONE,
                        getCompletionPerscent(arr));
                break;
        }

        return notification;
    }

    public boolean isActAvailableFromPPT(Long arrangementId) {
        try {
            Optional<Boolean> result = restTemplate.exchange(
                    createUri(arrangementId, PPT_IS_ACT_AVAILABLE_FOR_AUTOMATIC_SEND),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Optional<Boolean>>(){}
            ).getBody();
            //FIXME Как то много опшеналов...
            return Optional.ofNullable(result).map(res -> res.orElse(false)).orElse(false);
        } catch (Exception ex) {
            throw AS_15_8_DispatcherException.logAndGet(log, String.format("Ошибка отправки сообщения с запросом статуса доступности отправки акта мероприятию %d в ППТ", arrangementId));
        }
    }

    public void changeArrangementStatusToActSentPPT(Long arrangementId) {
        try {
            restTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(gatewayUrl).path(PPT_ACT_SENT_STATUS).queryParam("id", arrangementId).build().toString(), Boolean.class);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw AS_15_8_DispatcherException.logAndGet(log, String.format("Ошибка отправки сообщения с изменение статуса ACT_SENT %d в ППТ, код возврата %s", arrangementId, ex.getStatusCode()));
        }
    }

    public Long getInterruptViolationNumberFromPPT(Long arrangementId) {
        try {
            ResponseEntity<Long> result = restTemplate.getForEntity(
                    createUri(arrangementId, PPT_ARRANGEMENT_INTERRUPT_VIOLATION_NUMBER),
                    Long.class
            );

            if (result.getBody() != null) {
                return result.getBody();
            } else return null;

        } catch (Exception ex) {
            log.error("Ошибка запроса предельного числа проверок для прерывания мероприятия " + arrangementId + " из ППТ", ex);
            return null;
        }
    }

    private String createUri(Long arrangementId, String path) {
        return UriComponentsBuilder
                .fromHttpUrl(gatewayUrl)
                .path(path)
                .queryParam("id", arrangementId)
                .build()
                .toString();
    }

    public Long getCompletionPerscent(Arrangement arrangement) {
        /*Arrangement arrangement = arrangementRepo.findById(arrangementId).orElseThrow(() ->
                new AS_15_8_DispatcherException("Ошибка расчёта % выполнения для мероприятия id =  " + arrangementId));
        */
        return Optional.ofNullable(arrangement)
                .map(arr -> {
                    Long checkUnits = arrangement.getCheckUnitsCount();

                    if (checkUnits == null || checkUnits == 0)
                        return 0L;

                    long arrangementsCount = resultsKafkaService.getResultsCount(arrangement.getId());
                    return Math.min(arrangementsCount * 100 / checkUnits, 100);
                }).orElse(0L);
    }
}
