package kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import checkUnits.CheckUnitStatusNotification;
import control.ExecutorControlMessage;
import control.ExecutorControlMessage.ControlCommand;
import enums.AccessToolUnit;
import enums.CheckUnitJobResult;
import lombok.extern.slf4j.Slf4j;
import scripts.exceptions.Captcha_RobotScriptExecutionException;

@Component
@Slf4j
public class JobNotificationsProducer {

	private static JobNotificationsProducer instance;
	
	private KafkaTemplate<String, CheckUnitStatusNotification> notificationTemplate;
	
	private KafkaTemplate<String, ExecutorControlMessage> controlMessagesTemplate;
	
    @Value("${spring.kafka.notification-topic}")
    private String notificationsTopicName;
    
    @Value("${spring.kafka.control-topic}")
    private String controlTopicName;

	@Autowired
	public JobNotificationsProducer(
		KafkaTemplate<String, CheckUnitStatusNotification> notificationTemplate,
		KafkaTemplate<String, ExecutorControlMessage> controlMessagesTemplate,
		String notificationsTopicName,
		String controlTopicName) {
		
		this.notificationTemplate = notificationTemplate;
		this.controlMessagesTemplate = controlMessagesTemplate;
		this.notificationsTopicName = notificationsTopicName;
		this.controlTopicName = controlTopicName;
		instance = this;
	}
	
	public static JobNotificationsProducer getInstance() {
		return instance;
	}
	
	public void sendCheckJobErrorNotification(Long jobID, Throwable cause) {
		try {
			CheckUnitStatusNotification notification = new CheckUnitStatusNotification();
			notification.setJobID(jobID);
			if(cause instanceof Captcha_RobotScriptExecutionException)
				notification.setCheckUnitStatus(CheckUnitJobResult.CAPTCHA_DETECTED);
			else
				notification.setCheckUnitStatus(CheckUnitJobResult.INTERNAL_ERROR);
			
			Message<CheckUnitStatusNotification> message = MessageBuilder
	                .withPayload(notification)
	                .setHeader(KafkaHeaders.TOPIC, notificationsTopicName)
	                .build();
			
			ListenableFuture<SendResult<String, CheckUnitStatusNotification>> future = notificationTemplate.send(message);
		     
		    future.addCallback(new ListenableFutureCallback<SendResult<String, CheckUnitStatusNotification>>() {
		 
		        @Override
		        public void onSuccess(SendResult<String, CheckUnitStatusNotification> result) {
		        	CheckUnitStatusNotification mess = result.getProducerRecord().value();
		            log.info("Сообщение успешно отправлено: " + mess.getJobID() + ", " + mess.getCheckUnitStatus());
		        }
		        @Override
		        public void onFailure(Throwable ex) {
		        	throw new RuntimeException("Ошибка при отправке сообщения с уведомлением об ошибке", ex);
		        }
		    });
		    future.get();
		} catch (Exception ex) {
			throw new RuntimeException("Ошибка при отправке сообщения с уведомлением об ошибке", ex);
		}
	}
	
	public void sendStopExecutorsMessage(AccessToolUnit accessToolUnit) {
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
	}
	
}
