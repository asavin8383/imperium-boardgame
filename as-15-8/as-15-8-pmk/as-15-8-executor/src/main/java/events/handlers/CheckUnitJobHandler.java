package events.handlers;

import analysis.CheckUnitStatusNotification;
import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitKey;
import enums.CheckUnitJobResult;
import events.ExecutorChannels;
import execution.ExecutionJobResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import robots.exceptions.*;
import service.JobsService;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.CompletionException;

@Service
@EnableBinding(ExecutorChannels.class)
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CheckUnitJobHandler {

    private final ExecutorChannels executorChannels;
    private final JobsService jobsService;


    @StreamListener(ExecutorChannels.INPUT_JOBS)
    public void consumeCheckUnitJob(Message<CheckUnitJob> message){
        Integer partitionId = message.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID, Integer.class);
        CheckUnitKey key = message.getHeaders().get(KafkaHeaders.RECEIVED_MESSAGE_KEY, CheckUnitKey.class);
        LocalDateTime originalTime = getOriginalTime(message);
        if(key == null) {
            log.error("Ошибка! Пустой ключ у сообщения: " + message.getPayload().toString());
            return;
        }

        if(!jobsService.isJobActual(key.getArrangementId(), key.getVersion(), originalTime)) {
            log.info("\n   ---->>> Задание пропущено, т. к. оно остановлено: " + message.getPayload().toString() +
                    ", key: " + key +
                    ", partition: " + partitionId +
                    ", offset: " + message.getHeaders().get(KafkaHeaders.OFFSET, Long.class) +
                    ", time: " + originalTime.toString());
            return;
        }

        log.info("\n   ---->>> Принято задание: " + message.getPayload().toString() +
                ", key: " + key +
                ", partition: " + partitionId +
                ", offset: " + message.getHeaders().get(KafkaHeaders.OFFSET, Long.class) +
                ", time: " + originalTime.toString());

        String verificationName = "";
        Date startTime = new Date();
        try {
            CheckUnitJob job = message.getPayload();

            verificationName = "jobID = " + key.getJobId() +
                    " accessTool = " + job.getAccessTool() +
                    " checkUnit = " + job.getCheckUnit().getValue();
            ExecutionJobResult executionJobResult = jobsService.executeJob(key, job);
            sendExecutionResult(executionJobResult, key, partitionId, startTime);
        } catch (Exception ex) {
            processError(ex, key, message.getPayload(), verificationName, partitionId, startTime);
        }
    }

    private LocalDateTime getOriginalTime(Message message) {
        Long timestamp = message.getHeaders().get(KafkaHeaders.RECEIVED_TIMESTAMP, Long.class);
        if(timestamp != null) {
            return LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(timestamp),
                    TimeZone.getDefault().toZoneId()
                );
        } else {
            return LocalDateTime.MIN;
        }
    }

    private void processError(
            Exception ex,
            CheckUnitKey key,
            CheckUnitJob job,
            String verificationName,
            Integer partitionId,
            Date startTime) {
        Throwable te = ex;
        while(te.getCause() != null && te instanceof CompletionException) {
            te = te.getCause();
        }

        try {
            sendCheckJobErrorNotification(te, key, job.getCheckUnit(), partitionId, startTime);
        } catch (Exception sendEx) {
            log.error("Ошибка при отправке сообщения с ошибкой при выполнении задания на проверку запрещенного ресурса: "+verificationName, sendEx);
        }

        if (te instanceof Timeout_ExecutionException){
            log.info("Проверка {}, остановлена по таймауту {} секунд",  key.getJobId(), jobsService.getJobTimeout());
        }
        else if(te instanceof Cancel_ExecutionException) {
            log.info("Выполнение проверки остановлено: " + verificationName);
        }
        else if(te instanceof Captcha_ExecutionException) {
            log.warn("Выполнение проверки остановлено, обнаружена капча: " + verificationName);
        }
        else if(te instanceof BadIP_ExecutionExeption) {
            log.warn("Выполнение проверки остановлено, ПС(ПАСД) обнаружила подозрительный IP: " + verificationName);
        }
        else if(te instanceof ExecutionException) {
            log.error("Выполнение проверки завершено с ошибкой: "+verificationName, te);
        }
        else {
            log.error("Ошибка при выполнении задания на проверку запрещенного ресурса: " + key.getJobId(), te);
        }
    }

    /**
     * Метод отправки результата выполнения робота в тему Kafka
     * @param jobResult Результат выполнения робота
     */
    private void sendExecutionResult(ExecutionJobResult jobResult, CheckUnitKey key, Integer partitionId, Date startTime) throws RuntimeException {
        try {
            jobResult.setStartTime(startTime);
            Message<ExecutionJobResult> message = MessageBuilder
                    .withPayload(jobResult)
                    .setHeader(KafkaHeaders.PARTITION_ID, partitionId)
                    .setHeader(KafkaHeaders.MESSAGE_KEY, key)
                    .build();

            boolean send = executorChannels.executionResults().send(message);
            if(send)
                log.info("Сообщение успешно отправлено: " + key.getJobId() + ", " + jobResult.getCheckUnit().getValue());
        } catch (Exception ex) {
            throw new RuntimeException("Ошибка при отправке сообщения с результатами работы робота", ex);
        }
    }

    private void sendCheckJobErrorNotification(Throwable cause, CheckUnitKey key, CheckUnit checkUnit, Integer partitionId, Date startTime) {
        try {
            CheckUnitStatusNotification.CheckUnitStatusNotificationBuilder notificationBuilder = CheckUnitStatusNotification.builder();
            notificationBuilder
                    .checkUnit(checkUnit)
                    .startTime(startTime);
            if(cause instanceof Captcha_ExecutionException) {
                notificationBuilder.checkResult(CheckUnitJobResult.CAPTCHA_DETECTED);
            }
            else if(cause instanceof BadIP_ExecutionExeption) {
                notificationBuilder.checkResult(CheckUnitJobResult.BAD_IP);
            }
            else if(cause instanceof Timeout_ExecutionException) {
                notificationBuilder.checkResult(CheckUnitJobResult.TIMEOUT_ERROR);
                notificationBuilder.description("Работа робота остановлена по таймауту: " + jobsService.getJobTimeout() + " сек.");
            } else {
                notificationBuilder.checkResult(CheckUnitJobResult.INTERNAL_ERROR);
                StringWriter sw = new StringWriter();
                cause.printStackTrace(new PrintWriter(sw));
                notificationBuilder.description(sw.toString());
            }

            CheckUnitStatusNotification notification = notificationBuilder.build();
            Message<CheckUnitStatusNotification> message = MessageBuilder
                    .withPayload(notification)
                    .setHeader(KafkaHeaders.PARTITION_ID, partitionId)
                    .setHeader(KafkaHeaders.MESSAGE_KEY, key)
                    .build();

            boolean send = executorChannels.results().send(message);
            if(send)
                log.info("Сообщение успешно отправлено: " + key.getJobId() + ", " + notification.getCheckResult());
        } catch (Exception ex) {
            throw new RuntimeException("Ошибка при отправке сообщения с уведомлением об ошибке", ex);
        }
    }
}
