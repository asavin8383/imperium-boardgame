package service.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
import enums.AccessToolUnit;
import enums.CheckUnitJobResult;
import execution.ExecutionJobResult;
import lombok.extern.slf4j.Slf4j;
import robots.Robot;
import robots.exceptions.Captcha_RobotScriptExecutionException;
import robots.exceptions.RobotScriptExecutionException;
import robots.factory.RobotsFactory;
import robots.factory.RobotsFactoryRegistry;
import service.RobotsService;

/**
 * Сервис управления роботами Selenium
 * @author shabalinAI
 *
 */
@Service
@Slf4j
public class RobotsServiceImpl implements RobotsService {
	
	@Autowired
	private KafkaTemplate<String, ExecutionJobResult> executionResultTemplate;
	
	@Autowired
	private KafkaTemplate<String, CheckUnitStatusNotification> notificationTemplate;
	
    @Value("${spring.kafka.produce-topic}")
    private String executionResultTopicName;
	
    @Value("${spring.kafka.notification-topic}")
    private String notificationsTopicName;
	
    private volatile Map<AccessToolUnit, Set<Robot>> robots = new HashMap<>();
    
	@Override
	public void run(CheckUnitJob checkUnitJob) throws Captcha_RobotScriptExecutionException{
		String robotName = "";
		try {
			robotName = "jobID = " + checkUnitJob.getJobID() +
					" accessTool = " + checkUnitJob.getAccessToolUnit() +
					" checkUnit = " + checkUnitJob.getCheckUnit().getValue();
			
			RobotsFactory robotFactory = RobotsFactoryRegistry.getRobot(checkUnitJob.getAccessToolUnit());
			Robot robot = robotFactory.createScript(checkUnitJob.getAccessToolParameters());
			
			addRobotToRegistry(checkUnitJob.getAccessToolUnit(), robot);
			
			ExecutionJobResult message = null;
			log.info("Запуск робота: " + robotName);
			
			boolean isCaptcha = false;
			try{
				message = robot.run(checkUnitJob.getCheckUnit());
			} catch (Exception ex) {
				if(ex instanceof RobotScriptExecutionException) {
					if(ex instanceof Captcha_RobotScriptExecutionException)
						isCaptcha = true;
					throw (RobotScriptExecutionException)ex;
				} else
					throw new RobotScriptExecutionException("Ошибка при выполнении скрипта робота", ex);
			} finally {
				if(!isCaptcha && robot != null) {
					try {
						robot.close();
						removeRobotFromRegistry(checkUnitJob.getAccessToolUnit(), robot);
					} catch (IOException ex) {
						log.error("Ошибка при закрытии скрипта", ex);
					}
				}
			}
			message.setJobID(checkUnitJob.getJobID());
			message.setCheckUnit(checkUnitJob.getCheckUnit());
	        message.setAccessToolUnit(checkUnitJob.getAccessToolUnit());
	        
			if(message != null) {
				log.info("Робот успешно завершил работу: "+robotName);
				sendExecutionResult(message);
			}
		} catch(Exception ex) {
			try {
				sendCheckJobErrorNotification(checkUnitJob.getJobID(), ex);
			} catch (Exception sendEx) {
				log.error("Ошибка при отправке сообщения с ошибкой при выполнении задания на проверку запрещенного ресурса: "+robotName, ex);
			}
			if(ex instanceof Captcha_RobotScriptExecutionException) {
				log.warn("Робот остановлен, обнаружена капча: " + robotName);
				throw (Captcha_RobotScriptExecutionException)ex;
			} else if(ex instanceof RobotScriptExecutionException) {
				log.error("Робот завершил работу с ошибкой: "+robotName, ex);
			} else {
				log.error("Ошибка при выполнении задания на проверку запрещенного ресурса: "+checkUnitJob.getJobID(), ex);
			}
		}
	}
	
	private void addRobotToRegistry(AccessToolUnit accessToolUnit, Robot robot) {
		Set<Robot> robotsSet = robots.get(accessToolUnit);
		if(robotsSet == null)
			robotsSet = new HashSet<>();
		robotsSet.add(robot);
		robots.put(accessToolUnit, robotsSet);
	}
	
	private void removeRobotFromRegistry(AccessToolUnit accessToolUnit, Robot robot) {
		Set<Robot> robotsSet = robots.get(accessToolUnit);
		if(robotsSet == null)
			return;
		robotsSet.remove(robot);
		robots.put(accessToolUnit, robotsSet);
	}
	
	/**
	 * Метод отправки результата выполнения робота в тему Kafka
	 * @param jobResult Результат выполнения робота
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 * @throws RobotScriptExecutionException 
	 */
	protected void sendExecutionResult(ExecutionJobResult jobResult) throws RuntimeException {
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
			if(cause instanceof Captcha_RobotScriptExecutionException) {
				notification.setCheckUnitStatus(CheckUnitJobResult.CAPTCHA_DETECTED);
			} else {
				notification.setCheckUnitStatus(CheckUnitJobResult.INTERNAL_ERROR);
				StringWriter sw = new StringWriter();
				cause.printStackTrace(new PrintWriter(sw));
				notification.setDescription(sw.toString());
			}
			
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

	@Override
	public void destroyRobots(AccessToolUnit accessToolUnit) throws IOException {
		Set<Robot> robotsSet = robots.get(accessToolUnit);
		if(robotsSet != null && robotsSet.size() > 0) {
			log.info("\n\n----------------\n"
					+ accessToolUnit + ": oстановка активных роботов..."
					+ "\n-----------------------\n");
			for(Robot robot : robotsSet) {
				robot.close();
			}
			robots.remove(accessToolUnit);
			log.info("\n\n----------------\n"
					+ accessToolUnit + ": pоботы успешно остановлены"
					+ "\n-----------------------\n");
		}		
	}
}
