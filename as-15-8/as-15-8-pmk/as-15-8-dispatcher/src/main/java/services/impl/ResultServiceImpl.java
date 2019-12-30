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
import events.handlers.ResultsHandler;
import exceptions.AS_15_8_DispatcherException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.*;
import model.enums.CheckType;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.binder.kafka.streams.InteractiveQueryService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import repositories.ArrangementRepo;
import repositories.ResultRepo;
import restapi.ArrangementStatusProducer;
import restapi.ErdiChecker;
import services.AnalysisResultService;
import services.AnalysisResultServiceFactory;
import services.ResultService;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
@Slf4j
public class ResultServiceImpl implements ResultService {

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
                            ResultsHandler.RESULT_TABLE_NAME,
                            QueryableStoreTypes.keyValueStore()
                    );
            if(store == null)
                return;
            List<Arrangement> runningArrangements = arrangementRepo.findRunning();
            for(Arrangement arrangement : runningArrangements){
                AtomicInteger count = new AtomicInteger();
                getArrangementIterator(store, arrangement.getId()).forEachRemaining(obj -> count.getAndIncrement());

                if(arrangement.getCheckUnitsCount() <= count.longValue()) {
                    KeyValueIterator<CheckUnitKey, CheckUnitResult> resultsIterator = getArrangementIterator(store, arrangement.getId());
                    while (resultsIterator.hasNext()) {
                        CheckUnitResult checkUnitResult = resultsIterator.next().value;
                        try {
                            if (checkUnitResult instanceof AnalysisResult) {
                                saveJobResult((AnalysisResult) checkUnitResult);
                            } else if (checkUnitResult instanceof CheckUnitStatusNotification) {
                                CheckUnitStatusNotification notification = (CheckUnitStatusNotification) checkUnitResult;
                                updateJobStatus(notification.getJobID(), notification.getErdiID(), notification.getCheckResult(), notification.getDescription());
                            }
                        } catch (Exception ex){
                            try {
                                log.error("Ошибка при обработке сообщения с анализом результатов проверки: " + checkUnitResult.getJobID() + ", " + checkUnitResult.getCheckUnit().getValue(), ex);

                                StringWriter sw = new StringWriter();
                                ex.printStackTrace(new PrintWriter(sw));

                                Result jobResult = updateJobStatus(checkUnitResult.getJobID(), checkUnitResult.getCheckUnit().getContentId(), CheckUnitJobResult.INTERNAL_ERROR, sw.toString());
                                log.info("Мероприятие завешено с ошибками: " + jobResult.getArrangementId());
                                arrangementStatusProducer.sendArrangementStatusMessage(new ArrangementStatusNotification(jobResult.getArrangementId(), ArrangementEvents.FINISH));
                            } catch(Exception newEx) {
                                log.error("Ошибка при сохранении ошибочной обработки сообщения с анализом результатов проверки: " + checkUnitResult.getJobID() + ", " + checkUnitResult.getCheckUnit().getValue(), newEx);
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
        return store.range(
                new CheckUnitKey(arrangementId, Long.MIN_VALUE),
                new CheckUnitKey(arrangementId, Long.MAX_VALUE)
        );
    }

    @Override
    @Transactional
    public Result saveJobResult(AnalysisResult analysisResult) {
        try{
            Result result = findJobByID(analysisResult.getJobID());
            AnalysisResultService<? super AnalysisResult> service = AnalysisResultServiceFactory.getService(analysisResult.getClass());
            result.setEndDate(LocalDateTime.now());
            result.setResult(checkStatus(result.getErdiId(), analysisResult.getCheckResult()));

            if(analysisResult.getCheckResult().equals(CheckUnitJobResult.INTERNAL_ERROR)) {
                result.setCheckType(CheckType.ERROR);
                saveResultAsError(result, service.getErrorText(analysisResult));
            } else {
                result.setCheckType(service.getCheckType());
                DetailResult detailResult = service.createDetails(result, analysisResult);
                try {
                    entityManager.persist(detailResult);
                } catch (EntityExistsException ex){
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
                } catch (EntityExistsException ex){
                    entityManager.merge(resultScreenShot);
                }
            }
            return result;
        } catch (Exception ex) {
            try {
                log.error("Ошибка при обработке сообщения с анализом результатов проверки: " + analysisResult.getJobID() + ", " + analysisResult.getCheckUnit().getValue(), ex);

                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));

                updateJobStatus(analysisResult.getJobID(), analysisResult.getCheckUnit().getContentId(), CheckUnitJobResult.INTERNAL_ERROR, sw.toString());
            } catch(Exception newEx) {
                log.error("Ошибка при сохранении ошибочной обработки сообщения с анализом результатов проверки: " + analysisResult.getJobID() + ", " + analysisResult.getCheckUnit().getValue(), newEx);
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

    @Override
    @Transactional
    public Result updateJobStatus(Long jobID, Long erdiID, CheckUnitJobResult status, String description) {
        Result result = findJobByID(jobID);
        result.setResult(checkStatus(erdiID, status));
        result.setEndDate(LocalDateTime.now());
        if(status == CheckUnitJobResult.INTERNAL_ERROR || status == CheckUnitJobResult.TIMEOUT_ERROR) {
            result.setCheckType(CheckType.ERROR);
            saveResultAsError(result, description);
        } else {
            resultRepo.save(result);
        }
        return result;
    }

    private Result findJobByID(Long jobID) {
        return resultRepo.findById(jobID)
                .orElseThrow(() ->
                        new AS_15_8_DispatcherException("Ошибка! Задание не найдено! ID: " + jobID)
                );
    }

    private void saveResultAsError(Result result, String exText) {
        ErrorDetailResult errorDetailResult = new ErrorDetailResult();

        errorDetailResult.setResult(result);

        errorDetailResult.setError(exText);
        try{
            entityManager.persist(errorDetailResult);
        } catch (EntityExistsException ex){
            entityManager.merge(errorDetailResult);
        }
    }

    private CheckUnitJobResult checkStatus(Long erdiId, CheckUnitJobResult status){
        if(erdiChecker.checkErdiStatus(erdiId).equals(ErdiStatus.EXCLUDED))
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
    public Long getArrangementsCount(Long id) {

        final ReadOnlyKeyValueStore<CheckUnitKey, CheckUnitResult> store =
                interactiveQueryService.getQueryableStore(
                        ResultsHandler.RESULT_TABLE_NAME,
                        QueryableStoreTypes.keyValueStore()
                );
        if(store == null)
            throw new AS_15_8_DispatcherException("Ошибка чтения store из kafka!");

        KeyValueIterator<CheckUnitKey, CheckUnitResult> resultsIterator = store.range(
                new CheckUnitKey(id, Long.MIN_VALUE),
                new CheckUnitKey(id, Long.MAX_VALUE)
        );

        Long count = Long.valueOf(0);
        while(resultsIterator.hasNext()) {
            count++;
            resultsIterator.next();
        }

        return count;
    }
}
