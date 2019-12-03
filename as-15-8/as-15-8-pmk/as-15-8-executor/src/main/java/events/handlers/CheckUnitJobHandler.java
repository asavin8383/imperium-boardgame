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
        log.info("\n   ---->>> Принято задание: " + message.getPayload().toString() +
                ", partition: "+message.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID, Integer.class) +
                ", offset: "+message.getHeaders().get(KafkaHeaders.OFFSET, Long.class));
        String verificationName = "";

        Integer timeout = executorProperties.getExecutor().getTimeout();
        timeout = timeout != null ? timeout : DEFAULT_EXECUTOR_TIMEOUT;

        try {
            verificationName = "jobID = " + message.getPayload().getJobID() +
                    " accessTool = " + message.getPayload().getAccessTool() +
                    " checkUnit = " + message.getPayload().getCheckUnit().getValue();

            CheckUnitVerificationService service =
                    checkUnitVerificationServiceFactory
                            .getService(message.getPayload());

            ExecutionJobResult executionJobResult;

            if (service instanceof RobotsServiceImpl && timeout >= 0){
                executionJobResult = CompletableFuture
                        .supplyAsync(() -> service.run(message.getPayload()))
                        .applyToEither(timeoutAfter(timeout, TimeUnit.SECONDS), (result) -> result)
                        .exceptionally(throwable -> {
                            throw new CompletionException(throwable);
                        })
                .join();
            }
            else {
                executionJobResult = service.run(message.getPayload());
            }

            sendExecutionResult(executionJobResult);
        } catch (Exception ex) {
            Throwable te = ex instanceof CompletionException ? ex.getCause() : ex;

            if (te instanceof TimeoutException){
                log.info("Проверка {}, остановлена по таймауту {} секунд",  message.getPayload().getJobID(), timeout);
            }
            else if(te instanceof Cancel_ExecutionException) {
                log.info("Выполнение проверки остановлено: " + verificationName);
            }
            try {
                sendCheckJobErrorNotification(message.getPayload().getJobID(), message.getPayload().getCheckUnit().getContentId(), te);
            } catch (Exception sendEx) {
                log.error("Ошибка при отправке сообщения с ошибкой при выполнении задания на проверку запрещенного ресурса: "+verificationName, sendEx);
            }
            if(te instanceof Captcha_ExecutionException) {
                log.warn("Выполнение проверки остановлено, обнаружена капча: " + verificationName);
            } else if(te instanceof ExecutionException) {
                log.error("Выполнение проверки завершено с ошибкой: "+verificationName, te);
            }
            else {
                log.error("Ошибка при выполнении задания на проверку запрещенного ресурса: "+message.getPayload().getJobID(), te);
            }
        }
    }

    public <T> CompletableFuture<T> timeoutAfter(long timeout, TimeUnit unit) {
        CompletableFuture<T> result = new CompletableFuture<T>();
        ScheduledExecutorService timeoutService = Executors.newSingleThreadScheduledExecutor();
        timeoutService.schedule(() -> result.completeExceptionally(new TimeoutException()), timeout, unit);
        return result;
    }

    /**
     * Метод отправки результата выполнения робота в тему Kafka
     * @param jobResult Результат выполнения робота
     */
    private void sendExecutionResult(ExecutionJobResult jobResult) throws RuntimeException {
        try {
            Message<ExecutionJobResult> message = MessageBuilder
                    .withPayload(jobResult)
                    .build();

            boolean send = executorChannels.executionResults().send(message);
            if(send)
                log.info("Сообщение успешно отправлено: " + jobResult.getJobID() + ", " + jobResult.getCheckUnit().getValue());
        } catch (Exception ex) {
            throw new RuntimeException("Ошибка при отправке сообщения с результатами работы робота", ex);
        }
    }

    private void sendCheckJobErrorNotification(Long jobID, Long erdiId, Throwable cause) {
        try {
            CheckUnitStatusNotification notification = new CheckUnitStatusNotification();
            notification.setJobID(jobID);
            notification.setErdiID(erdiId);
            if(cause instanceof Captcha_ExecutionException) {
                notification.setCheckUnitStatus(CheckUnitJobResult.CAPTCHA_DETECTED);
            } else {
                notification.setCheckUnitStatus(CheckUnitJobResult.INTERNAL_ERROR);
                StringWriter sw = new StringWriter();
                cause.printStackTrace(new PrintWriter(sw));
                notification.setDescription(sw.toString());
            }

            Message<CheckUnitStatusNotification> message = MessageBuilder
                    .withPayload(notification)
                    .build();

            boolean send = executorChannels.notifications().send(message);
            if(send)
                log.info("Сообщение успешно отправлено: " + notification.getJobID() + ", " + notification.getCheckUnitStatus());
        } catch (Exception ex) {
            throw new RuntimeException("Ошибка при отправке сообщения с уведомлением об ошибке", ex);
        }
    }
}
