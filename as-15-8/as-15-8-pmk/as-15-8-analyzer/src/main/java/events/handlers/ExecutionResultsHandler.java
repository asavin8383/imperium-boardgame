package events.handlers;

import analysis.AnalysisResult;
import analysis.CheckUnitStatusNotification;
import checkUnits.CheckUnitKey;
import enums.CheckUnitJobResult;
import events.AnalyzerChannels;
import execution.ExecutionJobResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.context.annotation.DependsOn;
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
        Integer partitionId = message.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID, Integer.class);
        CheckUnitKey key = message.getHeaders().get(KafkaHeaders.RECEIVED_MESSAGE_KEY, CheckUnitKey.class);
        log.info("Принято задание на анализ: " +
                "ID: " + job.getJobID() +
                ", checkUnit: " + job.getCheckUnit().getValue() +
                ", key: " + key +
                ", partition: " + partitionId +
                ", offset: "+message.getHeaders().get(KafkaHeaders.OFFSET, Long.class));
        try {
            AnalyzerService<? super ExecutionJobResult> service = AnalyzerServiceFactory.getService(job.getClass());
            AnalysisResult analysisResult = service.analyzeResult(job);
            sendAnalysisResult(analysisResult, key, partitionId);
            log.info("Анализ результата проверки ПС/ПАСД выполнен успешно : " + job.getJobID() + ", " + job.getCheckUnit().getValue());
        } catch (Exception ex) {
            log.error("Ошибка при обработке задания на анализ результатов проверки ПС/ПАСД : " + job.getJobID() + ", " + job.getCheckUnit().getValue(), ex);
            sendErrorNotification(job.getJobID(), job.getCheckUnit().getContentId(), ex, key, partitionId);
        }
    }

    /**
     * Метод отправки результата выполнения анализа в тему Kafka
     * @param analysisResult Результат выполнения анализа
     */
    private void sendAnalysisResult(AnalysisResult analysisResult, CheckUnitKey key, Integer partitionId) throws RuntimeException {
        try {
            Message<AnalysisResult> message = MessageBuilder
                    .withPayload(analysisResult)
                    .setHeader(KafkaHeaders.PARTITION_ID, partitionId)
                    .setHeader(KafkaHeaders.MESSAGE_KEY, key)
                    .build();

            boolean send = analyzerChannels.output().send(message);
            if(send)
                log.info("Сообщение успешно отправлено: " + analysisResult.getJobID() + ", " + analysisResult.getCheckUnit().getValue());
        } catch (Exception ex) {
            throw new RuntimeException("Ошибка при отправке сообщения с результатами анализа", ex);
        }
    }

    private void sendErrorNotification(Long jobID, Long erdiId, Throwable cause, CheckUnitKey key, Integer partitionId) {
        try {
            StringWriter sw = new StringWriter();
            cause.printStackTrace(new PrintWriter(sw));
            CheckUnitStatusNotification notification = CheckUnitStatusNotification
                    .builder()
                    .jobID(jobID)
                    .erdiID(erdiId)
                    .checkResult(CheckUnitJobResult.INTERNAL_ERROR)
                    .description(sw.toString())
                    .build();

            Message<CheckUnitStatusNotification> message = MessageBuilder
                    .withPayload(notification)
                    .setHeader(KafkaHeaders.PARTITION_ID, partitionId)
                    .setHeader(KafkaHeaders.MESSAGE_KEY, key)
                    .build();

            boolean send = analyzerChannels.output().send(message);
            if(send)
                log.info("Сообщение успешно отправлено: " + notification.getJobID() + ", " + notification.getCheckResult());
        } catch (Exception ex) {
            throw new RuntimeException("Ошибка при отправке сообщения с уведомлением об ошибке", ex);
        }
    }
}
