package kafka;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.AcknowledgingConsumerAwareMessageListener;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties.AckMode;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.listener.adapter.FilteringMessageListenerAdapter;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import checkUnits.CheckUnitJob;
import control.ExecutorControlMessage;
import enums.AccessToolUnit;
import lombok.extern.slf4j.Slf4j;
import scripts.exceptions.Captcha_RobotScriptExecutionException;
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
	
	/*@Autowired
	private KafkaTemplate<String, ExecutorControlMessage> controlMessagesTemplate;*/
	
	@Value("${spring.kafka.consume-topic}")
	private String checkUnitJobsTopicName;
    
    @Value("${spring.kafka.control-topic}")
    private String controlTopicName;
	
	@PostConstruct
    void createCheckUnitJobsListeners() {
    	
    	for(AccessToolUnit accessTool : AccessToolUnit.values()) {
    		    
    		ConcurrentMessageListenerContainer<String, CheckUnitJob> container =
    				kafkaListenerContainerFactory.createContainer(checkUnitJobsTopicName);
	    	
    		container.getContainerProperties().setGroupId("exec_"+accessTool.name().toLowerCase());
    		container.getContainerProperties().setAckMode(AckMode.MANUAL_IMMEDIATE);
    		
	    	container.setupMessageListener(
	    		new FilteringMessageListenerAdapter<String, CheckUnitJob>(
	    			(AcknowledgingConsumerAwareMessageListener<String, CheckUnitJob>)(data, ack, consumer) -> {
	    				consumeCheckUnitJobMessage(data, ack);
	    			}, 
	    			record -> !record.value().getAccessToolUnit().equals(accessTool),
	    			true
	    		)
	    	);
	    	
	    	container.start();
	    	listenerContainers.put(accessTool, container);
    	}
    }
	
	private void consumeCheckUnitJobMessage(ConsumerRecord<String, CheckUnitJob> message, Acknowledgment ack) {
		log.info("Принято задание: " + message.value().toString()+", partition: "+message.partition()+", offset: "+message.offset());    				
		try {
			robotsService.run(message.value());
			ack.acknowledge();
		} catch (Captcha_RobotScriptExecutionException ex) {
			stopListeners(message.value().getAccessToolUnit());
		}
	}
	
	@KafkaListener(
		topics = "${spring.kafka.control-topic}",
		containerFactory = "controlMessagesListenerContainerFactory",
		groupId = "${spring.kafka.group}"
	)
    public void consumeControlMessage(ExecutorControlMessage controlMessage, Acknowledgment ack) {
		log.info("Принято управляющее сообщение: " + controlMessage.toString());
    	try {
    		switch(controlMessage.getCommand()) {
				case STOP:
					stopListeners(controlMessage.getAccessToolUnit());
					break;
				case START:
					startListeners(controlMessage.getAccessToolUnit());
					break;
				default:
					log.warn("Команда управляющего сообщения " + controlMessage.getCommand() + " не поддерживается");
					break;
    		}
    	} catch (Exception ex) {
    		log.error("Ошибка при обработке управляющего сообщения: " + controlMessage.toString(), ex);
    	}
    	ack.acknowledge();
    }
	
	private void stopListeners(AccessToolUnit accessToolUnit) {
		try {
			MessageListenerContainer jobsListenerContainer = listenerContainers.get(accessToolUnit);
			if(jobsListenerContainer.isRunning()) {
				jobsListenerContainer.stop();
				log.info("\n\n-------------------------------------------\n"+
						"Слушатель заданий на проверку "+accessToolUnit+" успешно остановлен"+
						"\n-------------------------------------------\n");
			}
		} catch(Exception ex) {
			log.error("Ошибка при остановке слушателей для " + accessToolUnit, ex);
		}
	}
	
	private void startListeners(AccessToolUnit accessToolUnit) {
		try {
			MessageListenerContainer jobsListenerContainer = listenerContainers.get(accessToolUnit);
			if(!jobsListenerContainer.isRunning()) {
				jobsListenerContainer.start();
				log.info("\n\n-------------------------------------------\n"+
						"Слушатель заданий на проверку "+accessToolUnit+" успешно запущен"+
						"\n-------------------------------------------\n");
			}
		} catch(Exception ex) {
			log.error("Ошибка при запуске слушателей для " + accessToolUnit, ex);
		}
	}
	
	/*private void sendStopExecutorsMessage(AccessToolUnit accessToolUnit) {
		try {
			ExecutorControlMessage controlMessage = new ExecutorControlMessage(accessToolUnit, ControlCommand.STOP);
			
			Message<ExecutorControlMessage> message = MessageBuilder
	                .withPayload(controlMessage)
	                .setHeader(KafkaHeaders.TOPIC, controlTopicName)
	                .build();
			
			ListenableFuture<SendResult<String, ExecutorControlMessage>> future = controlMessagesTemplate.send(message);
		     
		    future.addCallback(new ListenableFutureCallback<SendResult<String, ExecutorControlMessage>>() {
		 
		        @Override
		        public void onSuccess(SendResult<String, ExecutorControlMessage> result) {
		            log.info("Сообщение для остановки модулей выполнения проверок успешно отправлено");
		        }
		        @Override
		        public void onFailure(Throwable ex) {
		        	throw new RuntimeException("Ошибка при отправке сообщения для остановки модулей выполнения проверок", ex);
		        }
		    });
		    future.get();
		} catch (Exception ex) {
			throw new RuntimeException("Ошибка при отправке сообщения для остановки модулей выполнения проверок", ex);
		}
	}*/
}
