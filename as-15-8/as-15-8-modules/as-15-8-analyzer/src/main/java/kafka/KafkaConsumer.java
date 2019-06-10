package kafka;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.adapter.FilteringMessageListenerAdapter;
import org.springframework.stereotype.Service;

import enums.AccessToolUnit;
import execution.ExecutionJobResult;
import kafka.listeners.ExecutionJobResultsListener;

@Service
@DependsOn({"analyzerServiceFactory"})
public class KafkaConsumer {
    
	private Map<AccessToolUnit, MessageListenerContainer> listenerContainers = new HashMap<>();
	
	@Value("${spring.kafka.consume-topic}")
	private String executionResultsTopicName;
	
	@Autowired
	private KafkaProducer kafkaProducer;
	
	@Autowired
	private ConcurrentKafkaListenerContainerFactory<String, ExecutionJobResult> kafkaListenerContainerFactory;
	
	@PostConstruct
    void createExecutionJobResultsListeners() {
    	
    	for(AccessToolUnit accessTool : AccessToolUnit.values()) {
    		    	
    		ConcurrentMessageListenerContainer<String, ExecutionJobResult> container = 
    				kafkaListenerContainerFactory.createContainer(executionResultsTopicName);
	    	
	    	container.getContainerProperties().setGroupId("analyzer_"+accessTool.name().toLowerCase());
	    	container.getContainerProperties().setAckMode(AckMode.MANUAL);
	    	
	    	container.setupMessageListener(
	    		new FilteringMessageListenerAdapter<String, ExecutionJobResult>(
	    			new ExecutionJobResultsListener(kafkaProducer), 
	    			record -> !record.value().getAccessToolUnit().equals(accessTool),
	    			true
	    		)
	    	);
	    	
	    	container.start();
	    	listenerContainers.put(accessTool, container);
    	}
    }
}
