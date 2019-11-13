package events.handlers;

import arrangement.ArrangementStatusNotification;
import checkUnits.CheckUnitStatusNotification;
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
import services.ArrangementResultService;

/**
 * Created by san
 * Date: 04.11.2019
 * Обработка результатов выполнения задачи от executor'а
 */
@Slf4j
@Service
@EnableBinding(DispatcherChannels.class)
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class JobNotificationHandler {

    private final ArrangementResultService arrangementResultService;
    private final ArrangementStatusProducer arrangementStatusProducer;

    @StreamListener(DispatcherChannels.INPUT_JOB_NOTIFICATIONS)
    public void consumeJobNotifications(Message<CheckUnitStatusNotification> message) {
        CheckUnitStatusNotification notification = message.getPayload();
        log.info("\n   ---->>> Принято сообщение с уведомлением от проверки: " + notification.toString() +
                ", partition: "+message.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID, Integer.class) +
                ", offset: "+message.getHeaders().get(KafkaHeaders.OFFSET, Long.class));
        try {
            Result job = arrangementResultService.updateJobStatus(notification.getJobID(), notification.getCheckUnitStatus(), notification.getDescription());
            if(notification.getCheckUnitStatus() == CheckUnitJobResult.CAPTCHA_DETECTED) {
                ArrangementStatusNotification arrNotification = new ArrangementStatusNotification(job.getArrangementId(), ArrangementEvents.PAUSE);
                arrangementStatusProducer.sendArrangementStatusMessage(arrNotification);
            } else {
                ExecutionStatus status = arrangementResultService.checkArrangementStatus(job.getArrangementId());
                if(status == ExecutionStatus.FINISHED) {
                    log.info("Мероприятие успешно завешено: " + job.getArrangementId());
                    arrangementStatusProducer.sendArrangementStatusMessage(new ArrangementStatusNotification(job.getArrangementId(), ArrangementEvents.FINISH));
                }
            }
        } catch (Exception ex) {
            log.error("Ошибка при обработке уведомления от проверки: " + notification.getJobID() + ", " + notification.getCheckUnitStatus(), ex);
        }
    }
}
