package events.handlers;

import checkUnits.CheckUnitJob;
import enums.CheckUnitJobResult;
import enums.ErdiStatus;
import events.DispatcherChannels;
import exceptions.AS_15_8_DispatcherException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import restapi.ErdiChecker;
import services.impl.CheckUnitPersistingService;

/**
 * Обработчик входящих сообщений от рест-сервиса с мероприятиями для заполнения
 * Creation date: 06.08.2019
 * Author: asavin
 */
@Slf4j
@Service
@EnableBinding(DispatcherChannels.class)
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class CheckUnitJobHandler {

    private final DispatcherChannels dispatcherChannels;

    private final CheckUnitPersistingService checkUnitPersistingService;

    private final ErdiChecker erdiChecker;

    /**
     * Запись заданий в результаты и передача их в executor
     * @param message сообщение из ППМ с чек-юнитом для запуска проверки
     */
    @StreamListener(DispatcherChannels.INPUT_JOBS)
    public void createJobItems(Message<CheckUnitJob> message){
        CheckUnitJob checkUnitJob = message.getPayload();
        Integer partitionId = message.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID, Integer.class);
        log.info("\n   ---->>> Принято задание: " + checkUnitJob.toString() +
                ", partition: "+ partitionId +
                ", offset: "+message.getHeaders().get(KafkaHeaders.OFFSET, Long.class));
        try {
            ErdiStatus erdiStatus = erdiChecker.checkErdiStatus(message.getPayload().getCheckUnit().getContentId());
            if(!erdiStatus.equals(ErdiStatus.ACTIVE)) {
                checkUnitPersistingService.persistCheckUnitJob(message.getPayload(), CheckUnitJobResult.valueOf(erdiStatus.name()));
                log.info("Проверка исключена из списка выполнения, т.к является неактуальной: " + message.getPayload().getCheckUnit().getContentId());
            } else {
                Result result = checkUnitPersistingService.persistCheckUnitJob(message.getPayload(), CheckUnitJobResult.RUNNING);
                checkUnitJob.setJobID(result.getId());
                sendJobToExecutor(checkUnitJob, partitionId);
            }
        } catch (Exception ex) {
            log.error("Ошибка обработки задания на проверку чек-юнита " + checkUnitJob.toString() + " диспетчером", ex);
        }
    }

    /**
     * Метод отправки задания роботу в тему Kafka
     * @param checkUnitJob Задание на проверку чек-юнита
     */
    private void sendJobToExecutor(CheckUnitJob checkUnitJob, Integer partitionId) {
        try {
            Message<CheckUnitJob> message = MessageBuilder
                    .withPayload(checkUnitJob)
                    //.setHeader(KafkaHeaders.MESSAGE_KEY, key)
                    .setHeader(KafkaHeaders.PARTITION_ID, partitionId)
                    .build();

            boolean send = dispatcherChannels.outputJobs().send(message);
            if(send)
                log.info("Сообщение успешно отправлено: " + checkUnitJob.getJobID() + ", " + checkUnitJob.getCheckUnit().getValue());
        } catch (Exception ex) {
            throw new AS_15_8_DispatcherException("Ошибка при отправке задания роботам", ex);
        }
    }

}
