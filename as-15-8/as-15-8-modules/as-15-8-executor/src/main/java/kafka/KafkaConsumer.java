package kafka;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.listener.adapter.FilteringMessageListenerAdapter;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import checkUnits.CheckUnitJob;
import control.ExecutorControlMessage;
import control.ExecutorControlMessage.ControlCommand;
import enums.AccessToolUnit;
import kafka.listeners.CheckUnitJobsListener;
import lombok.extern.slf4j.Slf4j;
import service.RobotsService;

@Service
@Slf4j
@DependsOn({"robotsFactory"})
public class KafkaConsumer {

	private Map<AccessToolUnit, MessageListenerContainer> listenerContainers = new HashMap<>();
	
	@Autowired
	private RobotsService robotsService;
	
	@Autowired
	private ConcurrentKafkaListenerContainerFactory<String, CheckUnitJob> kafkaListenerContainerFactory;
	
	@Value("${spring.kafka.consume-topic}")
	private String checkUnitJobsTopicName;
	
	@Value("${robots.max-parallel-running}")
	private int maxRobotsParallelRunning;
	
	@PostConstruct
    void createCheckUnitJobsListeners() {
    	
    	for(AccessToolUnit accessTool : AccessToolUnit.values()) {
    		    
    		ConcurrentMessageListenerContainer<String, CheckUnitJob> container =
    				kafkaListenerContainerFactory.createContainer(checkUnitJobsTopicName);
	    	
    		container.getContainerProperties().setGroupId("exec_"+accessTool.name().toLowerCase());
    		container.getContainerProperties().setAckMode(AckMode.MANUAL);
	    	
	    	container.setupMessageListener(
	    		new FilteringMessageListenerAdapter<String, CheckUnitJob>(
	    			new CheckUnitJobsListener(robotsService, maxRobotsParallelRunning), 
	    			record -> !record.value().getAccessToolUnit().equals(accessTool)
	    		)
	    	);
	    	
	    	container.start();
	    	listenerContainers.put(accessTool, container);
    	}
    }
	
	@KafkaListener(
		topics = "${spring.kafka.control-topic}",
		containerFactory = "controlMessagesListenerContainerFactory",
		groupId = "${spring.kafka.group}"
	)
    public void consumeControlMessage(ExecutorControlMessage controlMessage, Acknowledgment ack) {
		log.info("Принято управляющее сообщение: " + controlMessage.toString());
        CompletableFuture.runAsync(() -> {
        	try {
        		MessageListenerContainer jobsListenerContainer = listenerContainers.get(controlMessage.getAccessToolUnit());
        		if(controlMessage.getCommand() == ControlCommand.STOP)
        			jobsListenerContainer.pause();
        		else if(controlMessage.getCommand() == ControlCommand.START)
        			jobsListenerContainer.resume();
        		
        		@SuppressWarnings("unchecked")
				CheckUnitJobsListener listener = (CheckUnitJobsListener)(
        				(FilteringMessageListenerAdapter<String, CheckUnitJob>)
	        				jobsListenerContainer
	        				.getContainerProperties()
	        				.getMessageListener()
        				).getDelegate();
        		
        		listener.stopRunningJobs();
        		log.info("Слушатель заданий на проверку "+controlMessage.getAccessToolUnit()+" успешно остановлен");
        	} catch (Exception ex) {
        		log.error("Ошибка при обработке управляющего сообщения: " + controlMessage.toString(), ex);
        	}
        	ack.acknowledge();
        });
    }
}
