package events.handlers;

import analysis.AnalysisResult;
import arrangement.ArrangementStatusNotification;
import enums.ArrangementEvents;
import enums.CheckUnitJobResult;
import enums.ExecutionStatus;
import events.DispatcherChannels;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;
import restapi.ArrangementStatusProducer;
import services.ResultService;

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

    private final ResultService resultService;
    private final ArrangementStatusProducer arrangementStatusProducer;

    @StreamListener(DispatcherChannels.INPUT_ANALYSIS_RESULTS)
    public void consumeAnalysisResults(Message<AnalysisResult> analysisResultMessage) {
        AnalysisResult analysisResult = analysisResultMessage.getPayload();
        log.info("\n   ---->>> Принято сообщение с анализом результатов проверки: " +
                analysisResult.getJobID() + ", " + analysisResult.getCheckUnit().getValue() + ", результат: " + analysisResult.getCheckResult() +
                ". partition: " + analysisResultMessage.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID, Integer.class) +
                ", offset: " + analysisResultMessage.getHeaders().get(KafkaHeaders.OFFSET, Long.class));
        try {
            Result jobResult = resultService.saveJobResult(analysisResult);
            log.info("Результаты выполнения проверки успешно обработаны: " + analysisResult.getJobID() + ", " + analysisResult.getCheckUnit().getValue());

            ExecutionStatus status = resultService.checkArrangementStatus(jobResult.getArrangementId());
            if(status == ExecutionStatus.FINISHED) {
                log.info("Мероприятие успешно завешено: " + jobResult.getArrangementId());
                arrangementStatusProducer.sendArrangementStatusMessage(new ArrangementStatusNotification(jobResult.getArrangementId(), ArrangementEvents.FINISH));
            }
        } catch (Exception ex) {
            try {
                log.error("Ошибка при обработке сообщения с анализом результатов проверки: " + analysisResult.getJobID() + ", " + analysisResult.getCheckUnit().getValue(), ex);

                StringWriter sw = new StringWriter();
                ex.printStackTrace(new PrintWriter(sw));

                Result jobResult = resultService.updateJobStatus(analysisResult.getJobID(), CheckUnitJobResult.INTERNAL_ERROR, sw.toString());
                ExecutionStatus status = resultService.checkArrangementStatus(jobResult.getArrangementId());
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
