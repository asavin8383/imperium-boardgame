package kafka;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.event.ConsumerStoppedEvent;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.DelegatingMessageListener;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import checkUnits.CheckUnitJob;
import control.ExecutorControlMessage;
import enums.AccessToolUnit;
import kafka.CheckUnitJobsListenerEndpoint.CheckUnitJobMessageListener;
import lombok.extern.slf4j.Slf4j;
import scripts.RobotScript;
import scripts.exceptions.Captcha_RobotScriptExecutionException;
import service.RobotsService;

@Service
@Slf4j
@DependsOn({"robotsFactory"})
public class KafkaConsumer {
	
	@Autowired
	private RobotsService robotsService;
	
	@Autowired
	private ConcurrentKafkaListenerContainerFactory<String, CheckUnitJob> kafkaListenerContainerFactory;
	
	@Autowired
	private KafkaListenerEndpointRegistry endpointRegistry;
	
	@Value("${spring.kafka.consume-topic}")
	private String checkUnitJobsTopicName;
    
    @Value("${spring.kafka.control-topic}")
    private String controlTopicName;
	
	@PostConstruct
    void createCheckUnitJobsListeners() {
		
    	for(AccessToolUnit accessTool : AccessToolUnit.values()) {
    		
    		CheckUnitJobsListenerEndpoint endpoint = new CheckUnitJobsListenerEndpoint(
				checkUnitJobsTopicName,
				accessTool,
				(script, data, ack) -> {
					consumeCheckUnitJobMessage(script, data, ack);
				}
    		);
    		
    		endpointRegistry.registerListenerContainer(endpoint, kafkaListenerContainerFactory);
    	}
    }
	
	private void consumeCheckUnitJobMessage(RobotScript script, ConsumerRecord<String, CheckUnitJob> message, Acknowledgment ack) {
		log.info("\n   ---->>> Принято задание: " + message.value().toString()+", partition: "+message.partition()+", offset: "+message.offset());
		try {
			robotsService.run(message.value(), script);
			ack.acknowledge();
		} catch (Captcha_RobotScriptExecutionException ex) {
			stopListeners(message.value().getAccessToolUnit());
		}
	}
	
	@KafkaListener(
		id = "control",
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
			MessageListenerContainer jobsListenerContainer = endpointRegistry.getListenerContainer(accessToolUnit.name());//listenerContainers.get(accessToolUnit);
			if(!jobsListenerContainer.isContainerPaused() && !jobsListenerContainer.isPauseRequested()) {
				jobsListenerContainer.pause();
				log.info("\n\n-------------------------------------------\n"+
						"Слушатель заданий на проверку "+accessToolUnit+" успешно остановлен"+
						"\n-------------------------------------------\n");
			} else {
				log.info("Слушатель заданий на проверку "+accessToolUnit+" уже остановлен");
			}
		} catch(Exception ex) {
			log.error("Ошибка при остановке слушателей для " + accessToolUnit, ex);
		}
	}
	
	private void startListeners(AccessToolUnit accessToolUnit) {
		try {
			MessageListenerContainer jobsListenerContainer = endpointRegistry.getListenerContainer(accessToolUnit.name());//listenerContainers.get(accessToolUnit);
			if(jobsListenerContainer.isContainerPaused() || jobsListenerContainer.isPauseRequested()) {
				jobsListenerContainer.resume();
				log.info("\n\n-------------------------------------------\n"+
						"Слушатель заданий на проверку "+accessToolUnit+" успешно запущен"+
						"\n-------------------------------------------\n");
			} else {
				log.info("Слушатель заданий на проверку "+accessToolUnit+" уже запущен");
			}
		} catch(Exception ex) {
			log.error("Ошибка при запуске слушателей для " + accessToolUnit, ex);
		}
	}
	
	@EventListener
	public void listenStopContanier(ConsumerStoppedEvent event) throws Exception {
		Object container = event.getSource();
		if(container instanceof ConcurrentMessageListenerContainer) {
			Object messageListener = ((ConcurrentMessageListenerContainer<?, ?>)container).getContainerProperties().getMessageListener();
			if(messageListener instanceof DelegatingMessageListener) {
				Object listenerDelegate = ((DelegatingMessageListener<?>)messageListener).getDelegate();
				if(listenerDelegate instanceof CheckUnitJobMessageListener) {
					((CheckUnitJobMessageListener)listenerDelegate).destroy();
				}
			}
		}
	}
}
