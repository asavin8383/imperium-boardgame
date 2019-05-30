package kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import checkUnits.CheckUnitJob;
import lombok.extern.slf4j.Slf4j;
import service.RobotsService;

@Service
@Slf4j
public class KafkaConsumer {

	@Autowired
	private RobotsService robotsService;
	
	@KafkaListener(topics = "${spring.kafka.consume-topic}")
    public void consumeJson(CheckUnitJob checkUnitJob, Acknowledgment ack) {
		log.info("Принято задание: " + checkUnitJob.toString());
        CompletableFuture.runAsync(() -> {
        	try {
        		robotsService.run(checkUnitJob);
        	} catch (Exception ex) {
        		log.error("Ошибка при обработке задания проверки запрещенного ресурса: " + checkUnitJob.toString(), ex);
        		JobNotificationsProducer.getInstance().sendCheckJobErrorNotification(checkUnitJob.getJobID(), ex);
        	}
        	ack.acknowledge();
        });
    }
	
}
