package events.handlers;

import analysis.CheckUnitStatusNotification;
import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitKey;
import common.ExecutorProperties;
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
import robots.exceptions.Cancel_ExecutionException;
import robots.exceptions.Captcha_ExecutionException;
import robots.exceptions.ExecutionException;
import robots.exceptions.Timeout_ExecutionException;
import service.CheckUnitVerificationService;
import service.CheckUnitVerificationServiceFactory;
import service.impl.RobotsServiceImpl;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.concurrent.*;

@Service
@EnableBinding(ExecutorChannels.class)
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CheckUnitJobHandler {

    private final ExecutorChannels executorChannels;

    private final CheckUnitVerificationServiceFactory checkUnitVerificationServiceFactory;

    private final ExecutorProperties executorProperties;

    private static final int DEFAULT_EXECUTOR_TIMEOUT = 60;


    @StreamListener(ExecutorChannels.INPUT_JOBS)
    public void consumeCheckUnitJob(Message<CheckUnitJob> message){
        Integer partitionId = message.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID, Integer.class);
        CheckUnitKey key = message.getHeaders().get(KafkaHeaders.RECEIVED_MESSAGE_KEY, CheckUnitKey.class);
        if(key == null){
            log.error("Ошибка! Пустой ключ у сообщения: " + message.getPayload().toString());
            return;
        }
        log.info("\n   ---->>> Принято задание: " + message.getPayload().toString() +
                ", key: " + key +
                ", partition: " + partitionId +
                ", offset: "+message.getHeaders().get(KafkaHeaders.OFFSET, Long.class));
        String verificationName = "";
        Date startTime = new Date();

        try {
            CheckUnitJob job = message.getPayload();
            verificationName = "jobID = " + key.getJobId() +
                    " accessTool = " + job.getAccessTool() +
                    " checkUnit = " + job.getCheckUnit().getValue();

            CheckUnitVerificationService service =
                    checkUnitVerificationServiceFactory
                            .getService(job);

            ExecutionJobResult executionJobResult;

            if (service instanceof RobotsServiceImpl && getTimeout() >= 0){
                executionJobResult = CompletableFuture
                        .supplyAsync(() -> service.run(key.getJobId(), job))
                        .applyToEither(timeoutAfter(getTimeout(), TimeUnit.SECONDS), (result) -> result)
                        .exceptionally(throwable -> {
                            service.stop();
                            throw new CompletionException(throwable);
                        })
                .join();
            }
            else {
                executionJobResult = service.run(key.getJobId(), job);
            }

            sendExecutionResult(executionJobResult, key, partitionId, startTime);
        } catch (Exception ex) {
            Throwable te = ex;
            while(te.getCause() != null && te instanceof CompletionException) {
                te = te.getCause();
            }

            try {
                sendCheckJobErrorNotification(te, key, message.getPayload().getCheckUnit(), partitionId, startTime);
            } catch (Exception sendEx) {
                log.error("Ошибка при отправке сообщения с ошибкой при выполнении задания на проверку запрещенного ресурса: "+verificationName, sendEx);
            }

            if (te instanceof Timeout_ExecutionException){
                log.info("Проверка {}, остановлена по таймауту {} секунд",  key.getJobId(), getTimeout());
            }
            else if(te instanceof Cancel_ExecutionException) {
                log.info("Выполнение проверки остановлено: " + verificationName);
            }
            else if(te instanceof Captcha_ExecutionException) {
                log.warn("Выполнение проверки остановлено, обнаружена капча: " + verificationName);
            }
            else if(te instanceof ExecutionException) {
                log.error("Выполнение проверки завершено с ошибкой: "+verificationName, te);
            }
            else {
                log.error("Ошибка при выполнении задания на проверку запрещенного ресурса: " + key.getJobId(), te);
            }
        }
    }

    private int getTimeout(){
        Integer timeout = executorProperties.getExecutor().getTimeout();
        timeout = timeout != null ? timeout : DEFAULT_EXECUTOR_TIMEOUT;
        return timeout;
    }

    public <T> CompletableFuture<T> timeoutAfter(long timeout, TimeUnit unit) {
        CompletableFuture<T> result = new CompletableFuture<>();
        ScheduledExecutorService timeoutService = Executors.newSingleThreadScheduledExecutor();
        timeoutService.schedule(() -> result.completeExceptionally(new Timeout_ExecutionException()), timeout, unit);
        return result;
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
            else if(cause instanceof Timeout_ExecutionException) {
                notificationBuilder.checkResult(CheckUnitJobResult.TIMEOUT_ERROR);
                notificationBuilder.description("Работа робота остановлена по таймауту: " + getTimeout() + " сек.");
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
