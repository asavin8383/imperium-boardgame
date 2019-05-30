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
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;

import checkUnits.CheckUnit;
import checkUnits.CheckUnitType;
import execution.ExecutionJobResult;
import kafka.KafkaConfiguration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import scripts.exceptions.RobotScriptExecutionException;

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
	private String jobID;
	
	@Getter
	private CheckUnit checkUnit;

	@Getter
	private String hubURL;

	@Getter
	private String platformName;

	@Getter
	private String applicationName;

	@Getter
	private String browserName;

	@Getter
	protected String vpnProxy;

	/**
	 * Метод создания драйвера
	 * @param hubURL URL хаба selenium
	 * @param browserName Имя браузера
	 * @param platformName Имя платформы
	 * @param applicationName Имя приложения (ПС/ПАСД)
	 * @throws MalformedURLException
	 */
	@BeforeClass
	@Parameters({"hubURL", "browserName", "platformName", "applicationName", "jobID", "checkUnitType", "checkUnitValue", "vpnProxy"})
	public void createDriver(
			String hubURL,
			String browserName,
			String platformName,
			String applicationName,
			String jobID,
			String checkUnitType,
			String checkUnitValue,
			@Optional String vpnProxy
		) throws MalformedURLException {
		
			this.driver = DriverFactory.createDriver(new URL(hubURL), platformName, applicationName, browserName, vpnProxy);
			this.jobID = jobID;
			this.checkUnit = new CheckUnit(CheckUnitType.valueOf(checkUnitType), checkUnitValue);

			this.hubURL = hubURL;
			this.platformName = platformName;
			this.applicationName = applicationName;
			this.browserName = browserName;
			this.vpnProxy = vpnProxy;
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
			throw new RobotScriptExecutionException("Ошибка при отправке сообщения с результатами работы робота", ex);
		}
	}
	
}
