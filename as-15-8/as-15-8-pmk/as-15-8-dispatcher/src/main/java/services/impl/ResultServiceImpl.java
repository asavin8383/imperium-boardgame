package services.impl;

import analysis.AnalysisResult;
import analysis.CheckUnitResult;
import analysis.CheckUnitStatusNotification;
import arrangement.ArrangementStatusNotification;
import checkUnits.CheckUnitKey;
import enums.ArrangementEvents;
import enums.CheckUnitJobResult;
import enums.ErdiStatus;
import enums.ExecutionStatus;
import exceptions.AS_15_8_DispatcherException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.*;
import model.enums.CheckType;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
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
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

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
                getArrangementIterator(store, arrangement.getId()).forEachRemaining(obj -> count.getAndIncrement());

                if(arrangement.getCheckUnitsCount() == count.longValue()) {
                    KeyValueIterator<CheckUnitKey, CheckUnitResult> resultsIterator = getArrangementIterator(store, arrangement.getId());
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

    private KeyValueIterator<CheckUnitKey, CheckUnitResult> getArrangementIterator(
            ReadOnlyKeyValueStore<CheckUnitKey, CheckUnitResult> store,
            Long arrangementId
    ){
        return getResultsIterator(store, arrangementId);
    }

    @Override
    @Transactional
    public Result saveJobResult(Arrangement arrangement, Long jobId, AnalysisResult analysisResult) {
        try{
            Result result = new Result();
            AnalysisResultService<? super AnalysisResult> service = AnalysisResultServiceFactory.getService(analysisResult.getClass());
            result.setArrangement(arrangement);
            result.setJobId(jobId);
            result.setErdiId(analysisResult.getCheckUnit().getContentId());
            result.setResult(checkStatus(result.getErdiId(), analysisResult.getCheckResult()));
            result.setCheckUnitType(analysisResult.getCheckUnit().getType());
            result.setCheckUnitValue(analysisResult.getCheckUnit().getValue());

            result.setStartDate(LocalDateTime.ofInstant(analysisResult.getStartTime().toInstant(), ZoneId.systemDefault()));
            result.setEndDate(LocalDateTime.ofInstant(analysisResult.getEndTime().toInstant(), ZoneId.systemDefault()));

            if(analysisResult.getCheckResult().equals(CheckUnitJobResult.INTERNAL_ERROR)) {
                result.setCheckType(CheckType.ERROR);
                saveResult(result);
                saveResultAsError(result, service.getErrorText(analysisResult));
            } else {
                result.setCheckType(service.getCheckType());
                saveResult(result);
                DetailResult detailResult = service.createDetails(result, analysisResult);
                try {
                    entityManager.persist(detailResult);
                } catch (Exception ex){
                    entityManager.merge(detailResult);
                }
            }
            if((analysisResult.getScreenshot() != null && analysisResult.getScreenshot().length > 0) ||
                    (analysisResult.getEtalonScreenshot() != null && analysisResult.getEtalonScreenshot().length > 0)){
                ResultScreenShot resultScreenShot = new ResultScreenShot();
                resultScreenShot.setResult(result);
                resultScreenShot.setScreenshot(analysisResult.getScreenshot());
                resultScreenShot.setEtalonScreenshot(analysisResult.getEtalonScreenshot());
                try {
                    entityManager.persist(resultScreenShot);
                } catch (Exception ex){
                    entityManager.merge(resultScreenShot);
                }
            }
            return result;
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
        return null;
    }

    @Override
    public ExecutionStatus checkArrangementStatus(Long arrangementID) {
        Long notFinishedJobsCount = resultRepo.countByResultNullOrResultIn(arrangementID,
                Arrays.asList(
                        CheckUnitJobResult.PLANNED,
                        CheckUnitJobResult.RUNNING)
        );
        return notFinishedJobsCount > 0 ? ExecutionStatus.RUNNING : ExecutionStatus.FINISHED;
    }

    private void saveResult(Result result){
        try {
            entityManager.persist(result);
        } catch (Exception ex){
            entityManager.merge(result);
        }
    }

    @Override
    @Transactional
    public Result updateJobStatus(Arrangement arrangement, Long jobId, CheckUnitResult checkUnitResult, CheckUnitJobResult status, String description) {
        Result result = new Result();
        result.setArrangement(arrangement);
        result.setJobId(jobId);
        result.setErdiId(checkUnitResult.getCheckUnit().getContentId());
        result.setResult(checkStatus(checkUnitResult.getCheckUnit().getContentId(), status));
        result.setCheckUnitType(checkUnitResult.getCheckUnit().getType());
        result.setCheckUnitValue(checkUnitResult.getCheckUnit().getValue());

        result.setStartDate(LocalDateTime.ofInstant(checkUnitResult.getStartTime().toInstant(), ZoneId.systemDefault()));
        result.setEndDate(LocalDateTime.ofInstant(checkUnitResult.getEndTime().toInstant(), ZoneId.systemDefault()));

        if(status == CheckUnitJobResult.INTERNAL_ERROR || status == CheckUnitJobResult.TIMEOUT_ERROR) {
            result.setCheckType(CheckType.ERROR);
            saveResult(result);
            saveResultAsError(result, description);
        } else {
            saveResult(result);
        }
        return result;
    }

    private void saveResultAsError(Result result, String exText) {
        ErrorDetailResult errorDetailResult = new ErrorDetailResult();

        errorDetailResult.setResult(result);

        errorDetailResult.setError(exText);
        try{
            entityManager.persist(errorDetailResult);
        } catch (Exception ex){
            entityManager.merge(errorDetailResult);
        }
    }

    private CheckUnitJobResult checkStatus(Long contentId, CheckUnitJobResult status){
        if(erdiChecker.checkErdiStatus(contentId).equals(ErdiStatus.EXCLUDED))
            return CheckUnitJobResult.EXCLUDED;
        else
            return status;
    }

    public void sendNotificationsIfFinished(Long arrangementId) {
        ExecutionStatus status = checkArrangementStatus(arrangementId);
        if(status == ExecutionStatus.FINISHED) {
            log.info("Мероприятие успешно завешено: " + arrangementId);
            arrangementStatusProducer.sendArrangementStatusMessage(new ArrangementStatusNotification(arrangementId, ArrangementEvents.FINISH));
        }
    }

    public ExecutionStatus getArrangementExecutionStatus(Long arrangementId) {
        return arrangementStatusProducer.getArrangementExcecutionStatus(arrangementId);
    }

    @Override
    public int getArrangementsCount(Long id) {
        ReadOnlyKeyValueStore<CheckUnitKey, CheckUnitResult> store = getKeyValueStore();
        KeyValueIterator<CheckUnitKey, CheckUnitResult> resultsIterator = getResultsIterator(store, id);
        return countIteratorSize(resultsIterator);
    }

    private ReadOnlyKeyValueStore<CheckUnitKey, CheckUnitResult> getKeyValueStore() {
        ReadOnlyKeyValueStore<CheckUnitKey, CheckUnitResult> store = interactiveQueryService.getQueryableStore(
                resultsTableName,
                QueryableStoreTypes.keyValueStore()
        );
        if(store == null)
            throw new AS_15_8_DispatcherException("Ошибка чтения store из kafka!");
        return store;
    }

    private KeyValueIterator<CheckUnitKey, CheckUnitResult> getResultsIterator(ReadOnlyKeyValueStore<CheckUnitKey, CheckUnitResult> store, Long id) {
        return store.range(
                new CheckUnitKey(id, Long.MIN_VALUE),
                new CheckUnitKey(id, Long.MAX_VALUE)
        );
    }

    private int countIteratorSize(KeyValueIterator<CheckUnitKey, CheckUnitResult> resultsIterator) {
        AtomicInteger count = new AtomicInteger();
        resultsIterator.forEachRemaining( s-> count.getAndIncrement());

        return count.get();
    }
}
