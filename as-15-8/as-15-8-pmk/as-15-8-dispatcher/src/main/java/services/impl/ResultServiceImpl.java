package services.impl;

import analysis.AnalysisResult;
import analysis.CheckUnitResult;
import analysis.CheckUnitStatusNotification;
import arrangement.ArrangementStatusNotification;
import checkUnits.CheckUnitKey;
import checkUnits.CheckUnitType;
import enums.ArrangementEvents;
import enums.CheckUnitJobResult;
import enums.ErdiStatus;
import exceptions.AS_15_8_DispatcherException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.*;
import model.enums.CheckType;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repositories.ArrangementRepo;
import repositories.ResultRepo;
import restapi.ArrangementStatusProducer;
import restapi.ErdiChecker;
import services.AnalysisResultService;
import services.AnalysisResultServiceFactory;
import services.ResultService;

import javax.persistence.EntityManager;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
@Slf4j
@Transactional
public class ResultServiceImpl implements ResultService {

    @Value("${spring.cloud.stream.bindings.results_table.destination}")
    private String resultsTableName;

    private final InteractiveQueryService interactiveQueryService;

    private final ArrangementRepo arrangementRepo;
    private final ResultRepo resultRepo;
    private final ErdiChecker erdiChecker;
    private final ArrangementStatusProducer arrangementStatusProducer;
    private final EntityManager entityManager;

    @Scheduled(cron = "${results.save.schedule}")
    public void saveResults(){
        try{
            final ReadOnlyKeyValueStore<CheckUnitKey, CheckUnitResult> store =
                    interactiveQueryService.getQueryableStore(
                            resultsTableName,
                            QueryableStoreTypes.keyValueStore()
                    );
            if(store == null)
                return;
            List<Arrangement> runningArrangements = arrangementRepo.findRunning();
            for(Arrangement arrangement : runningArrangements){
                AtomicInteger count = new AtomicInteger();
                getArrangementResultsIterator(store, arrangement.getId()).forEachRemaining(obj -> count.getAndIncrement());

                if(arrangement.getCheckUnitsCount() == count.longValue()) {
                    KeyValueIterator<CheckUnitKey, CheckUnitResult> resultsIterator = getArrangementResultsIterator(store, arrangement.getId());
                    while (resultsIterator.hasNext()) {
                        KeyValue<CheckUnitKey, CheckUnitResult> result = resultsIterator.next();
                        CheckUnitKey resultKey = result.key;
                        CheckUnitResult checkUnitResult = result.value;
                        try {
                            if (checkUnitResult instanceof AnalysisResult) {
                                saveJobResult(
                                        arrangement,
                                        resultKey.getJobId(),
                                        (AnalysisResult) checkUnitResult);
                            } else if (checkUnitResult instanceof CheckUnitStatusNotification) {
                                CheckUnitStatusNotification notification = (CheckUnitStatusNotification) checkUnitResult;
                                updateJobStatus(
                                        arrangement,
                                        resultKey.getJobId(),
                                        notification,
                                        notification.getCheckResult(),
                                        notification.getDescription());
                            }
                        } catch (Exception ex){
                            try {
                                log.error("Ошибка при обработке сообщения с анализом результатов проверки: " + resultKey.getJobId() + ", " + checkUnitResult.getCheckUnit().getValue(), ex);

                                StringWriter sw = new StringWriter();
                                ex.printStackTrace(new PrintWriter(sw));

                                Result jobResult = updateJobStatus(
                                        arrangement,
                                        resultKey.getJobId(),
                                        checkUnitResult,
                                        CheckUnitJobResult.INTERNAL_ERROR,
                                        sw.toString());
                                log.info("Мероприятие завешено с ошибками: " + jobResult.getArrangement().getId());
                                arrangementStatusProducer.sendArrangementStatusMessage(new ArrangementStatusNotification(jobResult.getArrangement().getId(), ArrangementEvents.FINISH));
                            } catch(Exception newEx) {
                                log.error("Ошибка при сохранении ошибочной обработки сообщения с анализом результатов проверки: " + resultKey.getJobId() + ", " + checkUnitResult.getCheckUnit().getValue(), newEx);
                                throw newEx;
                            }
                        }
                    }
                    log.info("Мероприятие успешно завешено: " + arrangement.getId());
                    arrangementStatusProducer.sendArrangementStatusMessage(new ArrangementStatusNotification(arrangement.getId(), ArrangementEvents.FINISH));
                }
            }
        } catch (Exception ex){
            log.error("Ошибка при сохранении результатов мероприятия", ex);
        }
    }

    @Override
    @Transactional
    public void saveJobResult(Arrangement arrangement, Long jobId, AnalysisResult analysisResult) {
        try{
            Result result = new Result();
            AnalysisResultService<? super AnalysisResult> service = AnalysisResultServiceFactory.getService(analysisResult.getClass());
            result.setArrangement(arrangement);
            result.setJobId(jobId);
            result.setErdiId(analysisResult.getCheckUnit().getContentId());
            result.setResult(checkErdiStatus(result.getErdiId(), analysisResult.getCheckResult()));
            result.setCheckUnitType(analysisResult.getCheckUnit().getType());
            result.setCheckUnitValue(analysisResult.getCheckUnit().getValue());

            result.setStartDate(LocalDateTime.ofInstant(analysisResult.getStartTime().toInstant(), ZoneId.systemDefault()));
            result.setEndDate(LocalDateTime.ofInstant(analysisResult.getEndTime().toInstant(), ZoneId.systemDefault()));

            if(analysisResult.getCheckResult().equals(CheckUnitJobResult.INTERNAL_ERROR)) {
                result.setCheckType(CheckType.ERROR);
                save(result);
                saveResultAsError(result, service.getErrorText(analysisResult));
            } else {
                result.setCheckType(service.getCheckType());
                save(result);
                DetailResult detailResult = service.createDetails(result, analysisResult);
                save(detailResult);
            }
            if((analysisResult.getScreenshot() != null && analysisResult.getScreenshot().length > 0) ||
                    (analysisResult.getEtalonScreenshot() != null && analysisResult.getEtalonScreenshot().length > 0)){
                ResultScreenShot resultScreenShot = new ResultScreenShot();
                resultScreenShot.setResult(result);
                resultScreenShot.setScreenshot(analysisResult.getScreenshot());
                resultScreenShot.setEtalonScreenshot(analysisResult.getEtalonScreenshot());
                save(resultScreenShot);
            }
        } catch (Exception ex) {
            try {
                log.error("Ошибка при обработке сообщения с анализом результатов проверки: " + jobId + ", " + analysisResult.getCheckUnit().getValue(), ex);

                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));

                updateJobStatus(
                        arrangement,
                        jobId,
                        analysisResult,
                        CheckUnitJobResult.INTERNAL_ERROR,
                        sw.toString());
            } catch(Exception newEx) {
                log.error("Ошибка при сохранении ошибочной обработки сообщения с анализом результатов проверки: " + jobId + ", " + analysisResult.getCheckUnit().getValue(), newEx);
            }
        }
    }

    @Override
    @Transactional
    public Result updateJobStatus(Arrangement arrangement, Long jobId, CheckUnitResult checkUnitResult, CheckUnitJobResult status, String description) {
        Result result = new Result();
        result.setArrangement(arrangement);
        result.setJobId(jobId);
        result.setErdiId(checkUnitResult.getCheckUnit().getContentId());
        result.setResult(checkErdiStatus(checkUnitResult.getCheckUnit().getContentId(), status));
        result.setCheckUnitType(checkUnitResult.getCheckUnit().getType());
        result.setCheckUnitValue(checkUnitResult.getCheckUnit().getValue());

        result.setStartDate(LocalDateTime.ofInstant(checkUnitResult.getStartTime().toInstant(), ZoneId.systemDefault()));
        result.setEndDate(LocalDateTime.ofInstant(checkUnitResult.getEndTime().toInstant(), ZoneId.systemDefault()));

        if(status == CheckUnitJobResult.INTERNAL_ERROR || status == CheckUnitJobResult.TIMEOUT_ERROR) {
            result.setCheckType(CheckType.ERROR);
            save(result);
            saveResultAsError(result, description);
        } else {
            save(result);
        }
        return result;
    }

    private void saveResultAsError(Result result, String exText) {
        ErrorDetailResult errorDetailResult = new ErrorDetailResult();
        errorDetailResult.setResult(result);
        errorDetailResult.setError(exText);
        save(errorDetailResult);
    }

    private CheckUnitJobResult checkErdiStatus(Long contentId, CheckUnitJobResult status){
        if(erdiChecker.checkErdiStatus(contentId).equals(ErdiStatus.EXCLUDED))
            return CheckUnitJobResult.EXCLUDED;
        else
            return status;
    }

    @Override
    public Page<CheckUnitResult> getArrangementResults(
            Long arrangementId,
            List<CheckUnitJobResult> checkUnitJobResults,
            List<CheckUnitType> checkUnitTypes,
            String query,
            Pageable pageable) {
        ReadOnlyKeyValueStore<CheckUnitKey, CheckUnitResult> store = getResultsKeyValueStore();
        KeyValueIterator<CheckUnitKey, CheckUnitResult> resultsIterator = getArrangementResultsIterator(store, arrangementId);
        List<CheckUnitResult> results = StreamSupport
                .stream(Spliterators.spliteratorUnknownSize(resultsIterator, Spliterator.ORDERED), true)
                .filter(kv -> {
                    boolean notFiltered = true;
                    if(checkUnitJobResults != null && checkUnitJobResults.size() > 0 &&
                            !checkUnitJobResults.contains(kv.value.getCheckResult())
                    )
                        notFiltered = false;
                    if(checkUnitTypes != null && checkUnitTypes.size() > 0 &&
                            !checkUnitTypes.contains(kv.value.getCheckUnit().getType())
                    )
                        notFiltered = false;
                    if(Strings.isNotEmpty(query) &&
                            !kv.value.getCheckResult().name().toLowerCase().contains(query.toLowerCase()) &&
                            !kv.value.getCheckUnit().getType().name().toLowerCase().contains(query.toLowerCase()) &&
                            !kv.value.getCheckUnit().getValue().toLowerCase().contains(query.toLowerCase())
                    )
                        notFiltered = false;
                    return notFiltered;
                })
                .map(kv -> kv.value)
                .collect(Collectors.toList());
        return new PageImpl<>(results, pageable, results.size());
    }

    @Override
    public CheckUnitResult getArrangementResult(Long arrangementId, Long resultId) {
        ReadOnlyKeyValueStore<CheckUnitKey, CheckUnitResult> store = getResultsKeyValueStore();
        return getArrangementResult(store, arrangementId, resultId);
    }

    @Override
    public long getResultsCount(Long arrangementId) {
        ReadOnlyKeyValueStore<CheckUnitKey, CheckUnitResult> store = getResultsKeyValueStore();
        return store.approximateNumEntries();
        /*KeyValueIterator<CheckUnitKey, CheckUnitResult> resultsIterator = getArrangementResultsIterator(store, arrangementId);
        return countIteratorSize(resultsIterator);*/
    }

    private KeyValueIterator<CheckUnitKey, CheckUnitResult> getArrangementResultsIterator(ReadOnlyKeyValueStore<CheckUnitKey, CheckUnitResult> store, Long arrangementId) {
        return store.range(
                new CheckUnitKey(arrangementId, Long.MIN_VALUE),
                new CheckUnitKey(arrangementId, Long.MAX_VALUE)
        );
    }

    private CheckUnitResult getArrangementResult(ReadOnlyKeyValueStore<CheckUnitKey, CheckUnitResult> store, Long arrangementId, Long jobId) {
        return store.get(new CheckUnitKey(arrangementId, Long.MIN_VALUE));
    }

    private int countIteratorSize(KeyValueIterator<CheckUnitKey, CheckUnitResult> resultsIterator) {
        AtomicInteger count = new AtomicInteger();
        resultsIterator.forEachRemaining( s-> count.getAndIncrement());
        return count.get();
    }

    private ReadOnlyKeyValueStore<CheckUnitKey, CheckUnitResult> getResultsKeyValueStore() {
        ReadOnlyKeyValueStore<CheckUnitKey, CheckUnitResult> store = interactiveQueryService.getQueryableStore(
                resultsTableName,
                QueryableStoreTypes.keyValueStore()
        );
        if(store == null)
            throw new AS_15_8_DispatcherException("Ошибка чтения store из kafka!");
        return store;
    }

    private void save(Object entity){
        try {
            entityManager.persist(entity);
        } catch (Exception ex){
            entityManager.merge(entity);
        }
    }
}
