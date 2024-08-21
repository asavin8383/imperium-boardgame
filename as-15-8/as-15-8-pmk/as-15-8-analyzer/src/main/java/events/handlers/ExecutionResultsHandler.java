package events.handlers;

import analysis.AnalysisResult;
import analysis.CheckUnitStatusNotification;
import checkUnits.CheckUnit;
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
import java.util.Date;

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
        assert key != null;

        log.info("Принято задание на анализ: ID: {}, checkUnit: {}, key: {}, partition: {}, offset: {}",
                key.getJobId(),
                job.getCheckUnit().getValue(),
                key,
                partitionId,
                message.getHeaders().get(KafkaHeaders.OFFSET, Long.class)
        );

        try {
            AnalyzerService<? super ExecutionJobResult> service = AnalyzerServiceFactory.getService(job.getClass());
            AnalysisResult analysisResult = service.analyzeResult(job);
            sendAnalysisResult(analysisResult, job.getStartTime(), key, partitionId);
            log.info("Анализ результата проверки ПС/ПАСД выполнен успешно : " + key.getJobId() + ", " + job.getCheckUnit().getValue());
        } catch (Exception ex) {
            log.error("Ошибка при обработке задания на анализ результатов проверки ПС/ПАСД : " + key.getJobId() + ", " + job.getCheckUnit().getValue(), ex);
            sendErrorNotification(ex, key, job.getCheckUnit(), job.getStartTime(), partitionId);
        }
    }

    /**
     * Метод отправки результата выполнения анализа в тему Kafka
     * @param analysisResult Результат выполнения анализа
     */
    private void sendAnalysisResult(AnalysisResult analysisResult, Date startTime, CheckUnitKey key, Integer partitionId) throws RuntimeException {
        try {
            analysisResult.setStartTime(startTime);
            Message<AnalysisResult> message = MessageBuilder
                    .withPayload(analysisResult)
                    .setHeader(KafkaHeaders.PARTITION_ID, partitionId)
                    .setHeader(KafkaHeaders.MESSAGE_KEY, key)
                    .build();

            boolean send = analyzerChannels.output().send(message);
            if(send)
                log.info("Сообщение успешно отправлено: " + key.getJobId() + ", " + analysisResult.getCheckUnit().getValue());
        } catch (Exception ex) {
            throw new RuntimeException("Ошибка при отправке сообщения с результатами анализа", ex);
        }
    }

    private void sendErrorNotification(Throwable cause, CheckUnitKey key, CheckUnit checkUnit, Date startTime, Integer partitionId) {
        try {
            StringWriter sw = new StringWriter();
            cause.printStackTrace(new PrintWriter(sw));
            CheckUnitStatusNotification notification = CheckUnitStatusNotification
                    .builder()
                    .checkUnit(checkUnit)
                    .checkResult(CheckUnitJobResult.INTERNAL_ERROR)
                    .startTime(startTime)
                    .description(sw.toString())
                    .build();

            Message<CheckUnitStatusNotification> message = MessageBuilder
                    .withPayload(notification)
                    .setHeader(KafkaHeaders.PARTITION_ID, partitionId)
                    .setHeader(KafkaHeaders.MESSAGE_KEY, key)
                    .build();

            boolean send = analyzerChannels.output().send(message);
            if(send)
                log.info("Сообщение успешно отправлено: " + key.getJobId() + ", " + notification.getCheckResult());
        } catch (Exception ex) {
            throw new RuntimeException("Ошибка при отправке сообщения с уведомлением об ошибке", ex);
        }
    }
}
