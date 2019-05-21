package kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import jobs.ArrangementJob;
import lombok.extern.slf4j.Slf4j;
import service.RobotsService;

@Service
@Slf4j
public class KafkaConsumer {

	@Autowired
	private RobotsService robotsService;
	
	@KafkaListener(topics = "${spring.kafka.consume-topic}")
    public void consumeJson(ArrangementJob arrangementJob, Acknowledgment ack) {
		log.info("Принято задание на проведение мероприятия: " + arrangementJob.getId());
        CompletableFuture.runAsync(() -> {
        	try {
        		robotsService.run(arrangementJob);
        	} catch (Exception ex) {
        		log.error("Ошибка при обработке задания на проведение мероприятия: " + arrangementJob.getId(), ex);
        	}
        	ack.acknowledge();
        });
    }
	
}
