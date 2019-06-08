package service.impl;

import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitStatusNotification;
import control.ExecutorControlMessage;
import control.ExecutorControlMessage.ControlCommand;
import enums.AccessToolUnit;
import enums.CheckUnitJobResult;
import execution.ExecutionJobResult;
import lombok.extern.slf4j.Slf4j;
import robots.Robot;
import robots.RobotsFactory;
import scripts.exceptions.Captcha_RobotScriptExecutionException;
import scripts.exceptions.RobotScriptExecutionException;
import service.RobotsService;

/**
 * Сервис управления роботами Selenium
 * @author shabalinAI
 *
 */
@Service
@Slf4j
public class SeleniumRobotsService implements RobotsService {
	
	@Autowired
	private KafkaTemplate<String, ExecutionJobResult> executionResultTemplate;
	
	@Autowired
	private KafkaTemplate<String, CheckUnitStatusNotification> notificationTemplate;
	
	@Autowired
	private KafkaTemplate<String, ExecutorControlMessage> controlMessagesTemplate;
	
    @Value("${spring.kafka.produce-topic}")
    private String executionResultTopicName;
	
    @Value("${spring.kafka.notification-topic}")
    private String notificationsTopicName;
    
    @Value("${spring.kafka.control-topic}")
    private String controlTopicName;
	
	@Override
	public void run(CheckUnitJob checkUnitJob) {
		try {
			String robotName = "jobID = " + checkUnitJob.getJobID() +
					" accessTool = " + checkUnitJob.getAccessToolUnit() +
					" checkUnit = " + checkUnitJob.getCheckUnit().getValue();
			
			Robot robot = RobotsFactory.getRobot(checkUnitJob.getAccessToolUnit());
			ExecutionJobResult message = null;
			log.info("Запуск робота: " + robotName);
			try {
				message = robot.run(checkUnitJob.getCheckUnit(), checkUnitJob.getAccessToolParameters());
				message.setJobID(checkUnitJob.getJobID());
				message.setCheckUnit(checkUnitJob.getCheckUnit());
		        message.setAccessToolUnit(checkUnitJob.getAccessToolUnit());
			} catch (Exception ex) {
				if(ex instanceof Captcha_RobotScriptExecutionException) {
					log.warn("Робот остановлен, обнаружена капча: " + robotName);
					sendStopExecutorsMessage(checkUnitJob.getAccessToolUnit());
				} else
					log.error("Робот завершил работу с ошибкой: "+robotName, ex);
				sendCheckJobErrorNotification(checkUnitJob.getJobID(), ex);
			}
			if(message != null) {
				log.info("Робот успешно завершил работу: "+robotName);
				try {
					sendExecutionResult(message);
				} catch(Exception ex) {
					log.error("Ошибка при отправке сообщения с результатами работы робота: "+robotName, ex);
				}
			}
		} catch(Exception ex) {
			log.error("Ошибка при выполнении задания на проверку запрещенного ресурса: "+checkUnitJob.getJobID(), ex);
		}
	}
	
	/**
	 * Метод отправки результата выполнения робота в тему Kafka
	 * @param jobResult Результат выполнения робота
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws RobotScriptExecutionException 
	 */
	protected void sendExecutionResult(ExecutionJobResult jobResult) throws RobotScriptExecutionException {
		try {
			Message<ExecutionJobResult> message = MessageBuilder
	                .withPayload(jobResult)
	                .setHeader(KafkaHeaders.TOPIC, executionResultTopicName)
	                .build();
			
			ListenableFuture<SendResult<String, ExecutionJobResult>> future = executionResultTemplate.send(message);
		     
		    future.addCallback(new ListenableFutureCallback<SendResult<String, ExecutionJobResult>>() {
		 
		        @Override
		        public void onSuccess(SendResult<String, ExecutionJobResult> result) {
		        	ExecutionJobResult mess = result.getProducerRecord().value();
		            log.info("Сообщение успешно отправлено: " + mess.getJobID() + ", " + mess.getCheckUnit().getValue());
		        }
		        @Override
		        public void onFailure(Throwable ex) {
		        	throw new RuntimeException(ex);
		        }
		    });
		    future.get();
		} catch (Exception ex) {
			throw new RuntimeException("Ошибка при отправке сообщения с результатами работы робота", ex);
		}
	}
	
	protected void sendCheckJobErrorNotification(Long jobID, Throwable cause) {
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
	
	protected void sendStopExecutorsMessage(AccessToolUnit accessToolUnit) {
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
