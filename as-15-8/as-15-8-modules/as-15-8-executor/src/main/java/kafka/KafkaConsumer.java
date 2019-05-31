package kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import checkUnits.CheckUnitJob;
import control.ExecutorControlMessage;
import control.ExecutorControlMessage.ControlCommand;
import lombok.extern.slf4j.Slf4j;
import service.RobotsService;

@Service
@Slf4j
public class KafkaConsumer {

	private static final String jobListenerID = "job_listener";
	
	@Autowired
	private RobotsService robotsService;
	
	@Autowired 
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;
	
	@KafkaListener(
		topics = "${spring.kafka.consume-topic}",
		id = jobListenerID,
		containerFactory = "kafkaListenerContainerFactory"
	)
    public void consumeCheckUnitJob(CheckUnitJob checkUnitJob, Acknowledgment ack) {
		log.info("Принято задание: " + checkUnitJob.toString());
       // CompletableFuture.runAsync(() -> {   // новый поток не нужен, чтобы работала пауза
    	try {
    		robotsService.run(checkUnitJob);
    	} catch (Exception ex) {
    		log.error("Ошибка при обработке задания проверки запрещенного ресурса: " + checkUnitJob.toString(), ex);
    		JobNotificationsProducer.getInstance().sendCheckJobErrorNotification(checkUnitJob.getJobID(), ex);
    	}
    	ack.acknowledge();
       // });
    }
	
	@KafkaListener(
		topics = "${spring.kafka.control-topic}",
		containerFactory = "controlListenerContainerFactory"
	)
    public void consumeControlMessage(ExecutorControlMessage controlMessage, Acknowledgment ack) {
		log.info("Принято управляющее сообщение: " + controlMessage.toString());
        CompletableFuture.runAsync(() -> {
        	try {
        		MessageListenerContainer jobsListener = kafkaListenerEndpointRegistry.getListenerContainer(jobListenerID);
        		if(controlMessage.getCommand() == ControlCommand.STOP)
        			jobsListener.pause();
        		else if(controlMessage.getCommand() == ControlCommand.START)
        			jobsListener.resume();
        	} catch (Exception ex) {
        		log.error("Ошибка при обработке управляющего сообщения: " + controlMessage.toString(), ex);
        	}
        	ack.acknowledge();
        });
    }
	
}
