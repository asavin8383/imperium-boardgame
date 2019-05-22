package scripts;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import execution.ExecutionJobResult;
import jobs.CheckUnit;
import jobs.CheckUnitType;
import kafka.KafkaConfiguration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Скрипт робота проверки ПС/ПАСД
 * @author shabalinAI
 *
 */
@Slf4j
@SpringBootTest
@ContextConfiguration(classes = {KafkaConfiguration.class})
public abstract class RobotScript extends AbstractTestNGSpringContextTests{

	/** Драйвер selenium */
	protected WebDriver driver;
	
	@Autowired
	private KafkaTemplate<String, ExecutionJobResult> executionResultTemplate;
	
	@Autowired
	private String executionResultTopicName;
	
	@Getter
	private String arrangenmentID;
	
	@Getter
	private String erdiID;
	
	@Getter
	private CheckUnit checkUnit;
	
	/**
	 * Метод создания драйвера
	 * @param hubURL URL хаба selenium
	 * @param browserName Имя браузера
	 * @param platformName Имя платформы
	 * @param applicationName Имя приложения (ПС/ПАСД)
	 * @param arrangenmentID Идентификатор мероприятия
	 * @param erdiID Идентификатор ЕРДИ
	 * @param url URL для проверки
	 * @throws MalformedURLException
	 */
	@BeforeClass
	@Parameters({"hubURL", "browserName", "platformName", "applicationName", "arrangenmentID", "erdiID", "checkUnitType", "checkUnitValue"})
	public void createDriver(
			String hubURL,
			String browserName,
			String platformName,
			String applicationName,
			String arrangenmentID,
			String erdiID,
			String checkUnitType,
			String checkUnitValue
		) throws MalformedURLException {
		
			this.driver = DriverFactory.createDriver(new URL(hubURL), platformName, applicationName, browserName);
			this.arrangenmentID = arrangenmentID;
			this.erdiID = erdiID;
			this.checkUnit = new CheckUnit(CheckUnitType.valueOf(checkUnitType), checkUnitValue);
	}
	
	/**
	 * Метод, запускаюший выполнение скрипта робота
	 */
	@Test
	public abstract void execute() throws RobotScriptExecutionException;
	
	/**
	 * Метод закрытия драйвера
	 */
	@AfterClass
	public void closeDriver() {
		if(driver!=null) {
			driver.quit();
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
		            log.info("Сообщение успешно отправлено: " +
		            		"arrangenmentID: " + result.getProducerRecord().value().getArrangenmentID() + ", " +
		            		"ERDI_ID: " + result.getProducerRecord().value().getErdiID() + ", " +
		            		"CheckUnit: " + result.getProducerRecord().value().getCheckUnit().getValue());
		        }
		        @Override
		        public void onFailure(Throwable ex) {
		        	throw new RuntimeException(ex);
		        }
		    });
		    future.get();
		} catch (Exception ex) {
			throw new RobotScriptExecutionException("Ошибка при отправке сообщения с результатами работы робота", ex);
		}
	}
	
}
