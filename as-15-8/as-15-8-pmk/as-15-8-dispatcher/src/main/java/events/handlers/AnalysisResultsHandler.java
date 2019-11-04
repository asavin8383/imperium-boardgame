package events.handlers;

import analysis.AnalysisResult;
import arrangement.ArrangementStatusNotification;
import enums.ArrangementEvents;
import enums.CheckUnitJobResult;
import enums.ExecutionStatus;
import events.DispatcherChannels;
import events.producers.rest.ArrangementStatusProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.ArrangementResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Service;
import services.ArrangementResultService;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by san
 * Date: 01.11.2019
 * Обработчик сообщений с результатми анализа проверяемых ресурсов
 */

@Slf4j
@Service
@EnableBinding(DispatcherChannels.class)
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class AnalysisResultsHandler {

    private final ArrangementResultService arrangementResultService;
    private final ArrangementStatusProducer arrangementStatusProducer;

    @StreamListener(DispatcherChannels.ANALYSIS_RESULTS_INPUT)
    public void consumeAnalysisResults(AnalysisResult analysisResult) {
        log.info("Принято сообщение с анализом результатов проверки: " + analysisResult.getJobID() + ", " + analysisResult.getCheckUnit().getValue());
        try {
            ArrangementResult jobResult = arrangementResultService.processJobResult(analysisResult);
            log.info("Результаты выполнения проверки успешно обработаны: " + analysisResult.getJobID() + ", " + analysisResult.getCheckUnit().getValue());

            ExecutionStatus status = arrangementResultService.checkArrangementStatus(jobResult.getArrangementId());
            if(status == ExecutionStatus.FINISHED) {
                log.info("Мероприятие успешно завешено: " + jobResult.getArrangementId());
                arrangementStatusProducer.sendArrangementStatusMessage(new ArrangementStatusNotification(jobResult.getArrangementId(), ArrangementEvents.FINISH));
            }

        } catch (Exception ex) {
            try {
                log.error("Ошибка при обработке сообщения с анализом результатов проверки: " + analysisResult.getJobID() + ", " + analysisResult.getCheckUnit().getValue(), ex);

                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));

                ArrangementResult jobResult = arrangementResultService.updateJobStatus(analysisResult.getJobID(), CheckUnitJobResult.INTERNAL_ERROR, sw.toString());
                ExecutionStatus status = arrangementResultService.checkArrangementStatus(jobResult.getArrangementId());
                if(status == ExecutionStatus.FINISHED) {
                    log.info("Мероприятие завешено с ошибками: " + jobResult.getArrangementId());
                    arrangementStatusProducer.sendArrangementStatusMessage(new ArrangementStatusNotification(jobResult.getArrangementId(), ArrangementEvents.FINISH));
                }
            } catch(Exception newEx) {
                log.error("Ошибка при сохранении ошибочной обработки сообщения с анализом результатов проверки: " + analysisResult.getJobID() + ", " + analysisResult.getCheckUnit().getValue(), newEx);
            }
        }
    }

}
