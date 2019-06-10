package kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import arrangement.ArrangementStatusNotification;
import checkUnits.CheckUnitStatusNotification;
import enums.ArrangementStatus;
import enums.CheckUnitJobResult;
import lombok.extern.slf4j.Slf4j;
import model.ArrangementResult;
import services.CheckUnitJobService;

/**
 * Creation date: 27.05.2019
 * Author: asavin
 * Обработчик результатов анализа
 */

@Service
@Slf4j
public class KafkaJobNotificationsConsumer {
	
	@Autowired
	private CheckUnitJobService checkUnitService;
	
	@Autowired
	private ArrangementStatusProducer arrangementStatusProducer;
	
    @KafkaListener(
    	topics = "${spring.kafka.notifications-topic}",
    	containerFactory = "jobNotificationsListenerContainerFactory"
    )
    public void consumeJobNotifications(CheckUnitStatusNotification notification, Acknowledgment ack) {
		log.info("Принято сообщение с уведомлением от проверки: " + notification.getJobID() + ", " + notification.getCheckUnitStatus());
    	try {       		
    		ArrangementResult job = checkUnitService.updateJobStatus(notification.getJobID(), notification.getCheckUnitStatus());
    		if(notification.getCheckUnitStatus() == CheckUnitJobResult.CAPTCHA_DETECTED) {
    			ArrangementStatusNotification arrNotification = new ArrangementStatusNotification(job.getArrangementId(), ArrangementStatus.ACTION_REQUIRED);
    			arrangementStatusProducer.sendArrangementStatusMessage(arrNotification);
    		} else {
    			ArrangementStatus arrStatus = checkUnitService.checkArrangementStatus(job.getArrangementId());
        		if(arrStatus == ArrangementStatus.FINISHED) {
        			log.info("Мероприятие успешно завешено: " + job.getArrangementId());
        			arrangementStatusProducer.sendArrangementStatusMessage(new ArrangementStatusNotification(job.getArrangementId(), arrStatus));
        		}
    		}
    	} catch (Exception ex) {
    		log.error("Ошибка при обработке уведомления от проверки: " + notification.getJobID() + ", " + notification.getCheckUnitStatus(), ex);
    	}
    	ack.acknowledge();
    }
    

}
