package kafka.listeners;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.AcknowledgingConsumerAwareMessageListener;
import org.springframework.kafka.support.Acknowledgment;

import checkUnits.CheckUnitJob;
import lombok.extern.slf4j.Slf4j;
import service.RobotsService;

@Slf4j
public class CheckUnitJobsListener implements AcknowledgingConsumerAwareMessageListener<String, CheckUnitJob> {

	private List<CompletableFuture<Void>> jobsFutures = new ArrayList<>();
	
	private RobotsService robotsService;
	
	private int maxRobotsParallelRunning;
	
	public CheckUnitJobsListener(RobotsService robotsService, int maxRobotsParallelRunning) {
		this.robotsService = robotsService;
		this.maxRobotsParallelRunning = maxRobotsParallelRunning;
	}
	
	@Override
	public void onMessage(ConsumerRecord<String, CheckUnitJob> data, Acknowledgment acknowledgment, Consumer<?, ?> consumer) {
		
		log.info("Принято задание: " + data.value().toString()+", partition: "+data.partition()+", offset: "+data.offset());
		
		clearDoneJobs();
		
		jobsFutures.add(CompletableFuture.runAsync(() -> {
    		robotsService.run(data.value());
    		acknowledgment.acknowledge();
		}));
		
		if(jobsFutures.size() >= maxRobotsParallelRunning) {
			CompletableFuture.allOf(jobsFutures.toArray(new CompletableFuture[jobsFutures.size()])).join();
			jobsFutures.clear();
		}
	}
	
	public void stopRunningJobs() {
		jobsFutures.forEach(future -> {
			if(!future.isDone())
				future.cancel(true);
		});
	}
	
	private void clearDoneJobs() {
		if(jobsFutures.size() > 0) {
			Iterator<CompletableFuture<Void>> i = jobsFutures.iterator();
			while(i.hasNext()) {
				if(i.next().isDone())
					i.remove();
			}
		}
	}
}
