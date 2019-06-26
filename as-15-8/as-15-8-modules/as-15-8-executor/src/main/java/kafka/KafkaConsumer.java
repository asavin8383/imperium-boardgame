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
import org.springframework.kafka.event.ConsumerStoppingEvent;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import checkUnits.CheckUnitJob;
import control.ExecutorControlMessage;
import enums.AccessToolUnit;
import lombok.extern.slf4j.Slf4j;
import robots.exceptions.Cancel_RobotScriptExecutionException;
import robots.exceptions.Captcha_RobotScriptExecutionException;
import service.RobotsService;

@Service
@Slf4j
@DependsOn({"robotsFactoryRegistry"})
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
				(data, ack) -> consumeCheckUnitJobMessage(data, ack)
    		);
    		
    		endpointRegistry.registerListenerContainer(endpoint, kafkaListenerContainerFactory);
    	};
    }
	
	private void consumeCheckUnitJobMessage(ConsumerRecord<String, CheckUnitJob> message, Acknowledgment ack) {
		log.info("\n   ---->>> Принято задание: " + message.value().toString()+", partition: "+message.partition()+", offset: "+message.offset());
		try {
			robotsService.run(message.value());
		} catch (Captcha_RobotScriptExecutionException ex) {
			stopListeners(message.value().getAccessToolUnit());
		} catch (Cancel_RobotScriptExecutionException e) {
			return;
		}
		ack.acknowledge();
	}
	
	@KafkaListener(
		topics = "${spring.kafka.control-topic}",
		containerFactory = "controlMessagesListenerContainerFactory",
		groupId = "#{execControlGroupID}"
	)
    public void consumeControlMessage(ExecutorControlMessage controlMessage, Acknowledgment ack) {
		ack.acknowledge();
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
    }
	
	private void stopListeners(AccessToolUnit accessToolUnit) {
		try {
			MessageListenerContainer jobsListenerContainer = endpointRegistry.getListenerContainer(accessToolUnit.name());
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
			MessageListenerContainer jobsListenerContainer = endpointRegistry.getListenerContainer(accessToolUnit.name());
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
	public void listenStopContanier(ConsumerStoppingEvent event) throws Exception {
		try {
			AccessToolUnit accessToolUnit = AccessToolUnit.valueOf(event.getContainer(ConcurrentMessageListenerContainer.class).getListenerId());
			if(accessToolUnit != null) {
				robotsService.destroyRobots(accessToolUnit);
			}
		} catch (Exception ex){}
	}
}
