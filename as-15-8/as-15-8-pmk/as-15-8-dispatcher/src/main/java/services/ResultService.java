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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
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
            List<Arrangement> runningArrangements = arrangementRepo.findRunning();

            for (Arrangement arrangement : runningArrangements) {
                AtomicInteger count = new AtomicInteger();
                resultsKafkaService.getArrangementResultsIterator(arrangement.getId())
                        .ifPresent(iter -> iter.forEachRemaining(obj -> count.getAndIncrement()));
                if(count.longValue() == 0)
                    break;

                if (arrangement.getCheckUnitsCount() == count.longValue()) {
                    log.info("Начато сохранение мероприятия: " + arrangement.getId());
                    resultsKafkaService.getArrangementResultsIterator(arrangement.getId())
                        .ifPresent(resultsIterator -> {
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
                                        log.error("Ошибка при сохранении результата проверки: " + resultKey.getJobId() + ", " + checkUnitResult.getCheckUnit().getValue(), ex);

                                        StringWriter sw = new StringWriter();
                                        ex.printStackTrace(new PrintWriter(sw));

                                        log.info("Сохраняем ошибку: " + resultKey.getJobId());
                                        saveJobStatus(
                                                arrangement,
                                                resultKey.getJobId(),
                                                checkUnitResult,
                                                CheckUnitJobResult.INTERNAL_ERROR,
                                                sw.toString());
                                        log.info("Ошибка сохранена: " + resultKey.getJobId());
                                    } catch (Exception newEx) {
                                        log.error("Ошибка при сохранении ошибочной обработки сообщения с анализом результатов проверки: " + resultKey.getJobId() + ", " + checkUnitResult.getCheckUnit().getValue(), newEx);
                                        isSaved = false;
                                        break;
                                    }
                                }
                            }
                            if (isSaved) {
                                log.info("Мероприятие успешно сохранено в БД: " + arrangement.getId());
                                arrangementStatusProducer.sendArrangementStatusMessage(new ArrangementStatusNotification(arrangement.getId(), ArrangementEvents.FINISH));
                                log.info("Мероприятие успешно завершено: " + arrangement.getId());
                            } else {
                                log.info("Ошибка сохранения мероприятия: " + arrangement.getId());
                            }
                        });
                }
            }
        } catch (Exception ex){
            log.error("Ошибка при сохранении результатов мероприятия", ex);
        }
    }

    private void saveJobResult(Arrangement arrangement, Long jobId, AnalysisResult analysisResult) {
        DetailResultService<? super CheckUnitResult> service = AnalysisResultServiceFactory.getService(analysisResult.getClass());
        Result result = resultRepo.findById(jobId).orElseGet(Result::new);
        log.info("Результат создан: " + result.toString());
        result.setArrangement(arrangement);
        resultsKafkaService.fillResult(result, jobId, analysisResult, service);
        //result = resultRepo.save(result);
        log.info("Результат сохранен: " + result.toString());
        DetailResult detailResult = service.create(result, analysisResult);
        result.setDetailResult(detailResult);
        //service.save(detailResult);
        log.info("Детальный результат сохранен: " + detailResult.toString());

        if((analysisResult.getScreenshot() != null && analysisResult.getScreenshot().length > 0) ||
                (analysisResult.getEtalonScreenshot() != null && analysisResult.getEtalonScreenshot().length > 0)){
            ResultScreenShot resultScreenShot = resultScreenShotRepo.findById(jobId).orElseGet(ResultScreenShot::new);
            resultScreenShot.setResult(result);
            resultScreenShot.setScreenshot(analysisResult.getScreenshot());
            resultScreenShot.setEtalonScreenshot(analysisResult.getEtalonScreenshot());
            log.info("Сохранение скриншота: " + resultScreenShot.getId());
            //resultScreenShotRepo.save(resultScreenShot);
            result.setResultScreenShot(resultScreenShot);
            log.info("Скриншот сохранен: " + resultScreenShot.getId());
        }
        resultRepo.save(result);
    }

    private void saveJobStatus(Arrangement arrangement, Long jobId, CheckUnitResult checkUnitResult, CheckUnitJobResult status, String description) {
        DetailResultService<? super CheckUnitResult> service = AnalysisResultServiceFactory.getService(checkUnitResult.getClass());
        Result result = resultRepo.findById(jobId).orElseGet(Result::new);
        result.setArrangement(arrangement);
        resultsKafkaService.fillResult(result, jobId, checkUnitResult, service);
        //result = resultRepo.save(result);

        if (status == CheckUnitJobResult.INTERNAL_ERROR || status == CheckUnitJobResult.TIMEOUT_ERROR) {
            DetailResult detailResult = service.create(result, checkUnitResult);
            result.setDetailResult(detailResult);
            //service.save(detailResult);
        }
        resultRepo.save(result);
    }
}
