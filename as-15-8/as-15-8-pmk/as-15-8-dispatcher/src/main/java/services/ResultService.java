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
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repositories.ArrangementRepo;
import repositories.ResultRepo;
import restapi.ArrangementStatusProducer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
@Transactional
public class ResultService {

    private final ResultsKafkaService resultsKafkaService;

    private final ArrangementRepo arrangementRepo;
    private final ResultRepo resultRepo;
    private final ArrangementStatusProducer arrangementStatusProducer;
    private final SessionFactory sessionFactory;

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

            try(Session session = sessionFactory.openSession()) {
                Transaction tx = session.beginTransaction();

                for (Arrangement arrangement : runningArrangements) {
                    AtomicInteger count = new AtomicInteger();
                    resultsKafkaService.getArrangementResultsIterator(store, arrangement.getId()).forEachRemaining(obj -> count.getAndIncrement());

                    if (arrangement.getCheckUnitsCount() == count.longValue()) {
                        KeyValueIterator<CheckUnitKey, CheckUnitResult> resultsIterator = resultsKafkaService.getArrangementResultsIterator(store, arrangement.getId());
                        while (resultsIterator.hasNext()) {
                            KeyValue<CheckUnitKey, CheckUnitResult> result = resultsIterator.next();
                            CheckUnitKey resultKey = result.key;
                            CheckUnitResult checkUnitResult = result.value;
                            try {
                                if (checkUnitResult instanceof AnalysisResult) {
                                    saveJobResult(
                                            session,
                                            arrangement,
                                            resultKey.getJobId(),
                                            (AnalysisResult) checkUnitResult);
                                } else if (checkUnitResult instanceof CheckUnitStatusNotification) {
                                    CheckUnitStatusNotification notification = (CheckUnitStatusNotification) checkUnitResult;
                                    saveJobStatus(
                                            session,
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
                                            session,
                                            arrangement,
                                            resultKey.getJobId(),
                                            checkUnitResult,
                                            CheckUnitJobResult.INTERNAL_ERROR,
                                            sw.toString());
                                    log.info("Мероприятие завешено с ошибками: " + jobResult.getArrangement().getId());
                                    arrangementStatusProducer.sendArrangementStatusMessage(new ArrangementStatusNotification(jobResult.getArrangement().getId(), ArrangementEvents.FINISH));
                                } catch (Exception newEx) {
                                    log.error("Ошибка при сохранении ошибочной обработки сообщения с анализом результатов проверки: " + resultKey.getJobId() + ", " + checkUnitResult.getCheckUnit().getValue(), newEx);
                                    throw newEx;
                                }
                            }
                        }
                        log.info("Мероприятие успешно сохранено в БД: " + arrangement.getId());
                        arrangementStatusProducer.sendArrangementStatusMessage(new ArrangementStatusNotification(arrangement.getId(), ArrangementEvents.FINISH));
                        log.info("Мероприятие успешно завершено: " + arrangement.getId());
                    }
                }
                tx.commit();
            }
        } catch (Exception ex){
            log.error("Ошибка при сохранении результатов мероприятия", ex);
        }
    }

    public void saveJobResult(Session session, Arrangement arrangement, Long jobId, AnalysisResult analysisResult) {
        try{
            AnalysisResultService<? super CheckUnitResult> service = AnalysisResultServiceFactory.getService(analysisResult.getClass());
            Result result = resultsKafkaService.createResult(jobId, analysisResult, service);
            result.setArrangement(arrangement);
            log.info("Результат подготовлен к сохранению: " + jobId + ", мероприятие: " + arrangement.getId());
            session.save(result);
            DetailResult detailResult = service.createDetails(result, analysisResult);
            session.save(detailResult);

            if((analysisResult.getScreenshot() != null && analysisResult.getScreenshot().length > 0) ||
                    (analysisResult.getEtalonScreenshot() != null && analysisResult.getEtalonScreenshot().length > 0)){
                ResultScreenShot resultScreenShot = new ResultScreenShot();
                resultScreenShot.setResult(result);
                resultScreenShot.setScreenshot(analysisResult.getScreenshot());
                resultScreenShot.setEtalonScreenshot(analysisResult.getEtalonScreenshot());
                session.save(resultScreenShot);
            }
        } catch (Exception ex) {
            try {
                log.error("Ошибка при обработке сообщения с анализом результатов проверки: " + jobId + ", " + analysisResult.getCheckUnit().getValue(), ex);

                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));

                saveJobStatus(
                        session,
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

    public Result saveJobStatus(Session session, Arrangement arrangement, Long jobId, CheckUnitResult checkUnitResult, CheckUnitJobResult status, String description) {
        AnalysisResultService<? super CheckUnitResult> service = AnalysisResultServiceFactory.getService(checkUnitResult.getClass());
        Result result = resultsKafkaService.createResult(jobId, checkUnitResult, service);
        result.setArrangement(arrangement);
        session.save(result);

        if(status == CheckUnitJobResult.INTERNAL_ERROR || status == CheckUnitJobResult.TIMEOUT_ERROR) {
            DetailResult detailResult = service.createDetails(result, checkUnitResult);
            session.save(detailResult);
        }
        return result;
    }
}
