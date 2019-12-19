package events.handlers;

import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitStatusNotification;
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
        Integer partitonId = message.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID, Integer.class);
        log.info("\n   ---->>> Принято задание: " + message.getPayload().toString() +
                ", partition: " + partitonId +
                ", offset: "+message.getHeaders().get(KafkaHeaders.OFFSET, Long.class));
        String verificationName = "";

        try {
            verificationName = "jobID = " + message.getPayload().getJobID() +
                    " accessTool = " + message.getPayload().getAccessTool() +
                    " checkUnit = " + message.getPayload().getCheckUnit().getValue();

            CheckUnitVerificationService service =
                    checkUnitVerificationServiceFactory
                            .getService(message.getPayload());

            ExecutionJobResult executionJobResult;

            if (service instanceof RobotsServiceImpl && getTimeout() >= 0){
                executionJobResult = CompletableFuture
                        .supplyAsync(() -> service.run(message.getPayload()))
                        .applyToEither(timeoutAfter(getTimeout(), TimeUnit.SECONDS), (result) -> result)
                        .exceptionally(throwable -> {
                            if (throwable instanceof TimeoutException){
                                service.stop();
                            }
                            throw new CompletionException(throwable);
                        })
                .join();
            }
            else {
                executionJobResult = service.run(message.getPayload());
            }

            sendExecutionResult(executionJobResult, partitonId);
        } catch (Exception ex) {
            Throwable te = ex;
            while(te.getCause() != null && te instanceof CompletionException) {
                te = te.getCause();
            }

            try {
                sendCheckJobErrorNotification(message.getPayload().getJobID(), message.getPayload().getCheckUnit().getContentId(), te, partitonId);
            } catch (Exception sendEx) {
                log.error("Ошибка при отправке сообщения с ошибкой при выполнении задания на проверку запрещенного ресурса: "+verificationName, sendEx);
            }

            if (te instanceof Timeout_ExecutionException){
                log.info("Проверка {}, остановлена по таймауту {} секунд",  message.getPayload().getJobID(), getTimeout());
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
                log.error("Ошибка при выполнении задания на проверку запрещенного ресурса: "+message.getPayload().getJobID(), te);
            }
        }
    }

    private int getTimeout(){
        Integer timeout = executorProperties.getExecutor().getTimeout();
        timeout = timeout != null ? timeout : DEFAULT_EXECUTOR_TIMEOUT;
        return timeout;
    }

    public <T> CompletableFuture<T> timeoutAfter(long timeout, TimeUnit unit) {
        CompletableFuture<T> result = new CompletableFuture<T>();
        ScheduledExecutorService timeoutService = Executors.newSingleThreadScheduledExecutor();
        timeoutService.schedule(() -> result.completeExceptionally(new Timeout_ExecutionException()), timeout, unit);
        return result;
    }

    /**
     * Метод отправки результата выполнения робота в тему Kafka
     * @param jobResult Результат выполнения робота
     */
    private void sendExecutionResult(ExecutionJobResult jobResult, Integer partitionId) throws RuntimeException {
        try {
            Message<ExecutionJobResult> message = MessageBuilder
                    .withPayload(jobResult)
                    .setHeader(KafkaHeaders.PARTITION_ID, partitionId)
                    .build();

            boolean send = executorChannels.executionResults().send(message);
            if(send)
                log.info("Сообщение успешно отправлено: " + jobResult.getJobID() + ", " + jobResult.getCheckUnit().getValue());
        } catch (Exception ex) {
            throw new RuntimeException("Ошибка при отправке сообщения с результатами работы робота", ex);
        }
    }

    private void sendCheckJobErrorNotification(Long jobID, Long erdiId, Throwable cause, Integer partitonId) {
        try {
            CheckUnitStatusNotification notification = new CheckUnitStatusNotification();
            notification.setJobID(jobID);
            notification.setErdiID(erdiId);
            if(cause instanceof Captcha_ExecutionException) {
                notification.setCheckUnitStatus(CheckUnitJobResult.CAPTCHA_DETECTED);
            }
            else if(cause instanceof Timeout_ExecutionException) {
                notification.setCheckUnitStatus(CheckUnitJobResult.TIMEOUT_ERROR);
                notification.setDescription("Работа робота остановлена по таймауту: " + getTimeout() + " сек.");
            } else {
                notification.setCheckUnitStatus(CheckUnitJobResult.INTERNAL_ERROR);
                StringWriter sw = new StringWriter();
                cause.printStackTrace(new PrintWriter(sw));
                notification.setDescription(sw.toString());
            }

            Message<CheckUnitStatusNotification> message = MessageBuilder
                    .withPayload(notification)
                    .setHeader(KafkaHeaders.PARTITION_ID, partitonId)
                    .build();

            boolean send = executorChannels.notifications().send(message);
            if(send)
                log.info("Сообщение успешно отправлено: " + notification.getJobID() + ", " + notification.getCheckUnitStatus());
        } catch (Exception ex) {
            throw new RuntimeException("Ошибка при отправке сообщения с уведомлением об ошибке", ex);
        }
    }
}
