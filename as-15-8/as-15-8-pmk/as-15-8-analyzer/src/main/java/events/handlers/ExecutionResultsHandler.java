package events.handlers;

import analysis.AnalysisResult;
import checkUnits.CheckUnitStatusNotification;
import enums.CheckUnitJobResult;
import events.AnalyzerChannels;
import execution.ExecutionJobResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import service.AnalyzerService;
import service.AnalyzerServiceFactory;

import java.io.PrintWriter;
import java.io.StringWriter;

@Service
@DependsOn({"analyzerServiceFactory"})
@EnableBinding(AnalyzerChannels.class)
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ExecutionResultsHandler {

    private final AnalyzerChannels analyzerChannels;

    @StreamListener(AnalyzerChannels.INPUT)
    public void consumeCheckUnitJob(Message<ExecutionJobResult> message){
        ExecutionJobResult job = message.getPayload();
        log.info("Принято задание на анализ: " +
                "ID: "+job.getJobID() +
                ", checkUnit: "+job.getCheckUnit().getValue() +
                ", partition: "+message.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID, String.class) +
                ", offset: "+message.getHeaders().get(KafkaHeaders.OFFSET, Long.class));
        try {
            AnalyzerService<? super ExecutionJobResult> service = AnalyzerServiceFactory.getService(job.getClass());
            AnalysisResult analysisResult = service.analyzeResult(job);
            sendAnalysisResult(analysisResult);
            log.info("Анализ результата проверки ПС/ПАСД выполнен успешно : " + job.getJobID() + ", " + job.getCheckUnit().getValue());
        } catch (Exception ex) {
            log.error("Ошибка при обработке задания на анализ результатов проверки ПС/ПАСД : " + job.getJobID() + ", " + job.getCheckUnit().getValue(), ex);
            sendErrorNotification(job.getJobID(), ex);
        }
    }

    /**
     * Метод отправки результата выполнения анализа в тему Kafka
     * @param analysisResult Результат выполнения анализа
     */
    private void sendAnalysisResult(AnalysisResult analysisResult) throws RuntimeException {
        try {
            Message<AnalysisResult> message = MessageBuilder
                    .withPayload(analysisResult)
                    .build();

            boolean send = analyzerChannels.output().send(message);
            if(send)
                log.info("Сообщение успешно отправлено: " + analysisResult.getJobID() + ", " + analysisResult.getCheckUnit().getValue());
        } catch (Exception ex) {
            throw new RuntimeException("Ошибка при отправке сообщения с результатами анализа", ex);
        }
    }

    private void sendErrorNotification(Long jobID, Throwable cause) {
        try {
            StringWriter sw = new StringWriter();
            cause.printStackTrace(new PrintWriter(sw));
            CheckUnitStatusNotification notification = new CheckUnitStatusNotification(jobID, CheckUnitJobResult.INTERNAL_ERROR, sw.toString());

            Message<CheckUnitStatusNotification> message = MessageBuilder
                    .withPayload(notification)
                    .build();

            boolean send = analyzerChannels.notifications().send(message);
            if(send)
                log.info("Сообщение успешно отправлено: " + notification.getJobID() + ", " + notification.getCheckUnitStatus());
        } catch (Exception ex) {
            throw new RuntimeException("Ошибка при отправке сообщения с уведомлением об ошибке", ex);
        }
    }
}
