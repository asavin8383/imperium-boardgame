package services;

import analysis.AnalysisResult;
import analysis.CheckUnitResult;
import analysis.CheckUnitStatusNotification;
import arrangement.ArrangementStatusNotification;
import checkUnits.CheckUnitKey;
import enums.ArrangementEvents;
import enums.CheckUnitJobResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Arrangement;
import model.DetailResult;
import model.Result;
import model.ResultScreenShot;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repositories.ArrangementRepo;
import repositories.ResultRepo;
import repositories.ResultScreenShotRepo;
import restapi.ArrangementStatusProducer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ResultService {

    private final ResultsKafkaService resultsKafkaService;

    private final ArrangementRepo arrangementRepo;
    private final ResultRepo resultRepo;
    private final ResultScreenShotRepo resultScreenShotRepo;
    private final ArrangementStatusProducer arrangementStatusProducer;

    @Scheduled(cron = "${results.save.schedule}")
    public void saveResults() {
        try{
            ReadOnlyKeyValueStore<CheckUnitKey, CheckUnitResult> store;
            try {
                store = resultsKafkaService.getResultsKeyValueStore();
            } catch (Exception ex) {
                return;
            }
            List<Arrangement> runningArrangements = arrangementRepo.findRunning();

            for (Arrangement arrangement : runningArrangements) {
                AtomicInteger count = new AtomicInteger();
                resultsKafkaService.getArrangementResultsIterator(store, arrangement.getId()).forEachRemaining(obj -> count.getAndIncrement());

                if (arrangement.getCheckUnitsCount() == count.longValue()) {
                    KeyValueIterator<CheckUnitKey, CheckUnitResult> resultsIterator = resultsKafkaService.getArrangementResultsIterator(store, arrangement.getId());
                    boolean isSaved = true;
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
                                saveJobStatus(
                                        arrangement,
                                        resultKey.getJobId(),
                                        notification,
                                        notification.getCheckResult(),
                                        notification.getDescription());
                            }
                        } catch (Exception ex) {
                            try {
                                log.error("Ошибка при обработке сообщения с анализом результатов проверки: " + resultKey.getJobId() + ", " + checkUnitResult.getCheckUnit().getValue(), ex);

                                StringWriter sw = new StringWriter();
                                ex.printStackTrace(new PrintWriter(sw));

                                Result jobResult = saveJobStatus(
                                        arrangement,
                                        resultKey.getJobId(),
                                        checkUnitResult,
                                        CheckUnitJobResult.INTERNAL_ERROR,
                                        sw.toString());
                            } catch (Exception newEx) {
                                log.error("Ошибка при сохранении ошибочной обработки сообщения с анализом результатов проверки: " + resultKey.getJobId() + ", " + checkUnitResult.getCheckUnit().getValue(), newEx);
                                isSaved = false;
                                break;
                            }
                        }
                    }
                    if(isSaved) {
                        log.info("Мероприятие успешно сохранено в БД: " + arrangement.getId());
                        arrangementStatusProducer.sendArrangementStatusMessage(new ArrangementStatusNotification(arrangement.getId(), ArrangementEvents.FINISH));
                        log.info("Мероприятие успешно завершено: " + arrangement.getId());
                    } else {
                        log.info("Ошибка сохранения мероприятия: " + arrangement.getId());
                        break;
                    }
                }
            }
        } catch (Exception ex){
            log.error("Ошибка при сохранении результатов мероприятия", ex);
        }
    }

    @Transactional
    void saveJobResult(Arrangement arrangement, Long jobId, AnalysisResult analysisResult) {
        try{
            DetailResultService<? super CheckUnitResult> service = AnalysisResultServiceFactory.getService(analysisResult.getClass());
            Result result = resultRepo.findById(jobId).orElseGet(Result::new);
            result.setArrangement(arrangement);
            resultsKafkaService.fillResult(result, jobId, analysisResult, service);
            resultRepo.save(result);
            DetailResult detailResult = service.create(result, analysisResult);
            service.save(detailResult);

            if((analysisResult.getScreenshot() != null && analysisResult.getScreenshot().length > 0) ||
                    (analysisResult.getEtalonScreenshot() != null && analysisResult.getEtalonScreenshot().length > 0)){
                ResultScreenShot resultScreenShot = resultScreenShotRepo.findById(jobId).orElseGet(ResultScreenShot::new);
                resultScreenShot.setResult(result);
                resultScreenShot.setScreenshot(analysisResult.getScreenshot());
                resultScreenShot.setEtalonScreenshot(analysisResult.getEtalonScreenshot());
                resultScreenShotRepo.save(resultScreenShot);
            }
        } catch (Exception ex) {
            log.error("Ошибка при сохранении результата проверки: " + jobId + ", " + analysisResult.getCheckUnit().getValue(), ex);
            throw ex;
        }
    }

    @Transactional
    Result saveJobStatus(Arrangement arrangement, Long jobId, CheckUnitResult checkUnitResult, CheckUnitJobResult status, String description) {
        try {
            DetailResultService<? super CheckUnitResult> service = AnalysisResultServiceFactory.getService(checkUnitResult.getClass());
            Result result = resultRepo.findById(jobId).orElseGet(Result::new);
            result.setArrangement(arrangement);
            resultsKafkaService.fillResult(result, jobId, checkUnitResult, service);
            resultRepo.save(result);

            if (status == CheckUnitJobResult.INTERNAL_ERROR || status == CheckUnitJobResult.TIMEOUT_ERROR) {
                DetailResult detailResult = service.create(result, checkUnitResult);
                service.save(detailResult);
            }
            return result;
        } catch (Exception ex) {
            log.error("Ошибка при сохранении статуса результата проверки: " + jobId + ", " + checkUnitResult.getCheckUnit().getValue(), ex);
            throw ex;
        }
    }
}
