package kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import jobs.ArrangementJob;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class KafkaConsumer {
	
	@KafkaListener(topics = "${spring.kafka.consume-topic}")
    public void consumeArrangementJob(ArrangementJob arrangementJob, Acknowledgment ack) {
		log.info("Принято задание на проведение мероприятия: " + arrangementJob.toString());
        CompletableFuture.runAsync(() -> {
        	try {
        		
        	} catch (Exception ex) {
        		log.error("Ошибка при обработке задания на проведение мероприятия: " + arrangementJob.toString(), ex);
        	}
        	ack.acknowledge();
        });
    }
	
}
