package services;

import analysis.AnalysisResult;
import analysis.CheckUnitResult;
import analysis.CheckUnitStatusNotification;
import checkUnits.CheckUnitKey;
import enums.CheckUnitJobResult;
import exceptions.AS_15_8_DispatcherException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.*;
import model.enums.ArrangementStatus;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ResultService {

    private final ResultsKafkaService resultsKafkaService;

    private final ArrangementRepo arrangementRepo;
    private final ResultRepo resultRepo;
    private final ResultScreenShotRepo resultScreenShotRepo;
    private final ArrangementStatusProducer arrangementStatusProducer;
    private final ArrangementService arrangementService;

    @Scheduled(cron = "${results.save.schedule}")
    public void saveCompletionArrangements() {
        try{
            arrangementRepo
                    .findReadyToUpload()
                    .stream()
                    .filter(arrangement -> {
                        long count = resultsKafkaService.getResultsCount(arrangement.getId());
                        if (count != 0 && arrangement.getCheckUnitsCount() <= count) {
                            arrangement.setStatus(ArrangementStatus.UPLOADING);
                            arrangementRepo.save(arrangement);
                            return true;
                        }
                        return false;
                    })
                    .forEach(this::saveArrangementResults);
        } catch (Exception ex){
            log.error("Ошибка при сохранении результатов мероприятий", ex);
        }
    }

    public void saveArrangementResults(Long arrangementId){
        Arrangement arrangement = arrangementRepo
                .findById(arrangementId)
                .orElseThrow((() -> new AS_15_8_DispatcherException("Ошибка! Мероприятие для выгрузки не найдено в БД по id: " + arrangementId)));
        arrangement.setStatus(ArrangementStatus.UPLOADING);
        arrangementRepo.save(arrangement);
        saveArrangementResults(arrangement);
    }

    private void saveArrangementResults(Arrangement arrangement) {
        log.info("Начато сохранение мероприятия: " + arrangement.getId());
        boolean isStopped = !arrangementService.isArrangementRunning(arrangement.getId(), arrangement.getVersion());
        try {
            resultsKafkaService.getArrangementResultsIterator(arrangement.getId())
                .ifPresent(resultsIterator -> {
                    boolean isSaved = true;
                    while (resultsIterator.hasNext()) {
                        KeyValue<CheckUnitKey, CheckUnitResult> result = resultsIterator.next();
                        isSaved = saveArrangementResult(result.key, result.value, arrangement);
                    }
                    if (isSaved) {
                        log.info("Мероприятие успешно сохранено в БД: " + arrangement.getId());
                        arrangementStatusProducer.sendArrangementStatusMessage(
                                arrangement.getId(),
                                arrangement.getVersion(),
                                isStopped ? ArrangementStatus.STOPPED : ArrangementStatus.FINISHED);
                        log.info("Мероприятие успешно завершено: " + arrangement.getId());
                    } else {
                        log.info("Ошибка сохранения мероприятия: " + arrangement.getId());
                    }
                });
        } catch (Exception ex){
            log.error("Ошибка при созранении результатов проверок мероприятия " + arrangement.getId(), ex);
            arrangement.setStatus(isStopped ? ArrangementStatus.STOPPING : ArrangementStatus.RUNNING);
            arrangementRepo.save(arrangement);
        }
    }

    private boolean saveArrangementResult(CheckUnitKey checkUnitKey, CheckUnitResult checkUnitResult, Arrangement arrangement) {
        try {
            if (checkUnitResult instanceof AnalysisResult) {
                saveJobResult(
                        arrangement,
                        checkUnitKey.getJobId(),
                        (AnalysisResult) checkUnitResult);
            } else if (checkUnitResult instanceof CheckUnitStatusNotification) {
                CheckUnitStatusNotification notification = (CheckUnitStatusNotification) checkUnitResult;
                saveJobStatus(
                        arrangement,
                        checkUnitKey.getJobId(),
                        notification,
                        notification.getCheckResult(),
                        notification.getDescription());
            }
        } catch (Exception ex) {
            try {
                log.error("Ошибка при сохранении результата проверки: " + checkUnitKey.getJobId() + ", " + checkUnitResult.getCheckUnit().getValue(), ex);

                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));

                saveJobStatus(
                        arrangement,
                        checkUnitKey.getJobId(),
                        checkUnitResult,
                        CheckUnitJobResult.INTERNAL_ERROR,
                        sw.toString());
                log.info("Ошибка сохранена: " + checkUnitKey.getJobId());
            } catch (Exception newEx) {
                log.error("Ошибка при сохранении ошибочной обработки сообщения с анализом результатов проверки: " + checkUnitKey.getJobId() + ", " + checkUnitResult.getCheckUnit().getValue(), newEx);
                return false;
            }
        }
        return true;
    }

    private void saveJobResult(Arrangement arrangement, Long jobId, AnalysisResult analysisResult) {
        DetailResultService<? super CheckUnitResult, ? extends DetailResult> service = AnalysisResultServiceFactory.getService(analysisResult.getClass());
        Result result = resultRepo.findById(jobId).orElseGet(Result::new);
        result.setArrangement(arrangement);
        resultsKafkaService.fillResult(result, jobId, analysisResult, service);
        DetailResult detailResult = service.getOrCreate(result, analysisResult);
        result.setDetailResult(detailResult);

        Optional<Screenshots> screenshotsOpt = resultsKafkaService.getScreenshot(arrangement.getId(), jobId);
        screenshotsOpt.ifPresent(screenshots -> {
            if((screenshots.getScreenshot() != null && screenshots.getScreenshot().length > 0) ||
                    (screenshots.getEtalonScreenshot() != null && screenshots.getEtalonScreenshot().length > 0)){
                ResultScreenShot resultScreenShot = resultScreenShotRepo.findById(jobId).orElseGet(ResultScreenShot::new);
                resultScreenShot.setResult(result);
                resultScreenShot.setScreenshot(screenshots.getScreenshot());
                resultScreenShot.setEtalonScreenshot(screenshots.getEtalonScreenshot());
                result.setResultScreenShot(resultScreenShot);
            }
        });
        resultRepo.save(result);
    }

    private void saveJobStatus(Arrangement arrangement, Long jobId, CheckUnitResult checkUnitResult, CheckUnitJobResult status, String description) {
        DetailResultService<? super CheckUnitResult, ? extends DetailResult> service = AnalysisResultServiceFactory.getService(checkUnitResult.getClass());
        Result result = resultRepo.findById(jobId).orElseGet(Result::new);
        result.setArrangement(arrangement);
        resultsKafkaService.fillResult(result, jobId, checkUnitResult, service);

        if (status == CheckUnitJobResult.INTERNAL_ERROR || status == CheckUnitJobResult.TIMEOUT_ERROR) {
            DetailResult detailResult = service.getOrCreate(result, checkUnitResult);
            result.setDetailResult(detailResult);
        }
        resultRepo.save(result);
    }
}
