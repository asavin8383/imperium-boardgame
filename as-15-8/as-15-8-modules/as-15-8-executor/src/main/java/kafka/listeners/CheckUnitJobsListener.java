package kafka.listeners;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.AcknowledgingConsumerAwareMessageListener;
import org.springframework.kafka.support.Acknowledgment;

import checkUnits.CheckUnitJob;
import lombok.extern.slf4j.Slf4j;
import service.RobotsService;

@Slf4j
public class CheckUnitJobsListener implements AcknowledgingConsumerAwareMessageListener<String, CheckUnitJob> {
	
	private RobotsService robotsService;
	
	public CheckUnitJobsListener(RobotsService robotsService) {
		this.robotsService = robotsService;
	}
	
	@Override
	public void onMessage(ConsumerRecord<String, CheckUnitJob> data, Acknowledgment acknowledgment, Consumer<?, ?> consumer) {
		
		log.info("Принято задание: " + data.value().toString()+", partition: "+data.partition()+", offset: "+data.offset());
		
    	robotsService.run(data.value());
    	acknowledgment.acknowledge();
	}
}
