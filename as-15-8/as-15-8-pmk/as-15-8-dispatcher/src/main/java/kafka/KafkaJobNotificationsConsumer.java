package kafka;

import arrangement.ArrangementStatusNotification;
import checkUnits.CheckUnitStatusNotification;
import enums.ArrangementEvents;
import enums.CheckUnitJobResult;
import enums.ExecutionStatus;
import events.producers.rest.ArrangementStatusProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.ArrangementResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import services.ArrangementResultService;

/**
 * Creation date: 27.05.2019
 * Author: asavin
 * Обработчик результатов анализа
 */

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class KafkaJobNotificationsConsumer {
	
	private final ArrangementResultService arrangementResultService;
	private final ArrangementStatusProducer arrangementStatusProducer;
	
    @KafkaListener(
    	topics = "${spring.kafka.notifications-topic}",
    	containerFactory = "jobNotificationsListenerContainerFactory"
    )
    public void consumeJobNotifications(CheckUnitStatusNotification notification, Acknowledgment ack) {
		log.info("Принято сообщение с уведомлением от проверки: " + notification.getJobID() + ", " + notification.getCheckUnitStatus());
    	try {       		
    		ArrangementResult job = arrangementResultService.updateJobStatus(notification.getJobID(), notification.getCheckUnitStatus(), notification.getDescription());
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
    	ack.acknowledge();
    }
    

}
