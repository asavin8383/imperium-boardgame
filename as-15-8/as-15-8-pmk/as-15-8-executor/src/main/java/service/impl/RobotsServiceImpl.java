package service.impl;

import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import common.ExecutorProperties;
import enums.AccessToolUnit;
import execution.ExecutionJobResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriverException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.stereotype.Service;
import robots.Robot;
import robots.exceptions.BadIP_ExecutionExeption;
import robots.exceptions.Captcha_ExecutionException;
import robots.exceptions.ExecutionException;
import robots.exceptions.InternalError_ExecutionException;
import robots.factory.RobotsFactory;
import service.CheckUnitVerificationService;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис управления роботами Selenium
 * @author shabalinAI
 *
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RobotsServiceImpl implements CheckUnitVerificationService {
	
	/** Список роботов */
	private final RobotsFactory robotsFactory;

	private final ExecutorProperties executorProps;
	
    private volatile Set<Robot> robots = ConcurrentHashMap.newKeySet();
    
    private boolean isRunning = false;

	@Value("${retryExecution.maxAttempts}")
	private int retryAttempts;
	@Value("${retryExecution.maxDelay}")
	private int retryDelay;
	long webdriverTimeout;

	@PostConstruct
	private void countTimeout() {
		//Рассчитываем таймаут исходя из таймаута сервиса executor
		long timeout = 30;
		if (retryAttempts > 0) {
			long countTimeout = (long) ((Optional.ofNullable(executorProps.getExecutor().getTimeout()).orElse(180) * 0.9 - (retryAttempts - 1) * retryDelay) / retryAttempts);
			if (countTimeout > timeout) {
				timeout = countTimeout;

			}
		}
		webdriverTimeout = timeout;
		log.info("Рассчитанный таймаут одной проверки составит {} секунд", webdriverTimeout);
	}


    public Map<AccessToolUnit, List<CheckUnitType>> getSupportedTypes() {
    	return new HashMap<AccessToolUnit, List<CheckUnitType>>(){{
    		put(AccessToolUnit.SEARCH_SYSTEM, Arrays.asList(CheckUnitType.URL, CheckUnitType.DOMAIN, CheckUnitType.SEARCH_PHRASE));
			put(AccessToolUnit.PROXY, Arrays.asList(CheckUnitType.URL, CheckUnitType.DOMAIN));
			put(AccessToolUnit.VPN, Arrays.asList(CheckUnitType.URL, CheckUnitType.DOMAIN));
			put(AccessToolUnit.CAMELEO_XYZ, Collections.singletonList(CheckUnitType.URL));
			put(AccessToolUnit.ANONYMIZER, Collections.singletonList(CheckUnitType.URL));
			put(AccessToolUnit.HIDEMYASS, Collections.singletonList(CheckUnitType.URL));
			put(AccessToolUnit.HOLA, Collections.singletonList(CheckUnitType.URL));
			put(AccessToolUnit.EXTENSION, Collections.singletonList(CheckUnitType.URL));
			put(AccessToolUnit.GOOGLE_API, Arrays.asList(CheckUnitType.URL, CheckUnitType.DOMAIN, CheckUnitType.SEARCH_PHRASE));
		}};
    }

	@Override
	public ExecutionJobResult run(Long jobId, CheckUnitJob checkUnitJob) throws ExecutionException {
		try {
			String robotName = "jobID = " + jobId +
                    " accessTool = " + checkUnitJob.getAccessTool() +
                    " checkUnit = " + checkUnitJob.getCheckUnit().getValue();
			/*if(!this.isRunning)
				throw new ExecutionException("Ошибка при запуске проверки запрещенного ресурса. Сервис проверки остановлен!");*/
			log.info("Запуск робота: " + robotName);

			Robot robot = robotsFactory.createRobot(checkUnitJob.getAccessTool());
			robot.setRemainingAttempts(retryAttempts);
			
			robots.add(robot);
			
			ExecutionJobResult message;
			boolean needToStop = true;
			try{
				message = runWithRetry(robot, checkUnitJob.getCheckUnit());
			} catch (Exception ex) {
				if(ex instanceof ExecutionException) {
					if(ex instanceof Captcha_ExecutionException  || ex instanceof BadIP_ExecutionExeption) {
						needToStop = false;
					}
					throw (ExecutionException)ex;
				} else
					throw new ExecutionException("Ошибка при выполнении скрипта робота", ex);
			} finally {
				if(needToStop && robot != null) {
					try {
						robot.destroy();
					} catch (IOException ex) {
						log.error("Ошибка при закрытии скрипта", ex);
					}
				}
			}
			robots.remove(robot);
			message.setCheckUnit(checkUnitJob.getCheckUnit());
	        message.setAccessTool(checkUnitJob.getAccessTool());

			log.info("Робот успешно завершил работу: "+robotName);
			return message;
		} catch(Exception ex) {
            if(ex instanceof ExecutionException)
                throw ex;
            else
                throw new ExecutionException("Ошибка при выполнении скрипта робота", ex);
		}
	}

	public ExecutionJobResult runWithRetry(Robot robot, CheckUnit checkUnit) {
		try {
			robot.setRemainingAttempts(robot.getRemainingAttempts() - 1);
			log.info("Запуск {}-й попытки проверки ресурса: {}", retryAttempts - robot.getRemainingAttempts(), checkUnit.getValue());
			boolean throwExceptionByCaptchaOrBadIP = true;
			if (robot.getRemainingAttempts()==0) {
				throwExceptionByCaptchaOrBadIP = false;
			}
			return robot.run(checkUnit, webdriverTimeout, throwExceptionByCaptchaOrBadIP);
		} catch (Captcha_ExecutionException | BadIP_ExecutionExeption | WebDriverException |
				 InternalError_ExecutionException ex) {
			if (robot.getRemainingAttempts() > 0) {
				log.warn("Будет выполнен {}-й перезапуск проверки ресурса {} по следующей причине: {}", retryAttempts - robot.getRemainingAttempts(), checkUnit.getValue(), ex.getMessage());
				try {
					Thread.sleep(retryDelay * 1000);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}
				try {
					robot.destroy();
				} catch (IOException e) {
					log.error("Ошибка при закрытии скрипта", e);
				}
				return runWithRetry(robot, checkUnit);
			} else {
				throw ex;
			}

		}
	}
	
	@Override
	public void start() {
		this.isRunning = true;
	}

	@Override
	public void stop() {
		isRunning = false;
		log.info("\n\n-----------------------------\n"
				+ "Oстановка активных роботов..."
				+ "\n-----------------------------\n");
		for(Robot robot : robots) {
			try {
				robot.destroy();
			} catch (IOException ex) {
				log.error("Ошибка при остановке робота", ex);
			}
		}
		log.info("\n\n-----------------------------\n"
				+ "Pоботы успешно остановлены"
				+ "\n-----------------------------\n");
	}

	@Override
	public boolean isRunning() {
		return this.isRunning;
	}
	
	@Override
	public int getPhase() {
		return AbstractMessageListenerContainer.DEFAULT_PHASE - 10;
	}
}
