package service.impl;

import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import enums.AccessToolUnit;
import execution.ExecutionJobResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.stereotype.Service;
import robots.Robot;
import robots.exceptions.Captcha_ExecutionException;
import robots.exceptions.ExecutionException;
import robots.factory.RobotsFactory;
import service.CheckUnitVerificationService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис управления роботами Selenium
 * @author shabalinAI
 *
 */
@Service
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RobotsServiceImpl implements CheckUnitVerificationService {
	
	/** Список роботов */
	private final RobotsFactory robotsFactory;
	
    private final Map<Long, Robot> robots = new ConcurrentHashMap<>();
    
    private boolean isRunning = false;

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
			
			robots.put(jobId, robot);

			ExecutionJobResult message;
			boolean needToStop = true;
			try{
				message = robot.run(checkUnitJob.getCheckUnit());
			} catch (Exception ex) {
				if(ex instanceof ExecutionException) {
					if(ex instanceof Captcha_ExecutionException)
						needToStop = false;
					throw (ExecutionException)ex;
				} else
					throw new ExecutionException("Ошибка при выполнении скрипта робота", ex);
			} finally {
				//if(needToStop && robot != null) {
				if(robot == null)
					log.info("Робот пустой: " + Thread.currentThread().getId());
				try {
					robot.destroy();
					robots.remove(jobId);
					log.info("Робот был закрыт: " + Thread.currentThread().getId());
				} catch (IOException ex) {
					log.error("Ошибка при закрытии скрипта", ex);
				}
				//}
			}
			message.setCheckUnit(checkUnitJob.getCheckUnit());
	        message.setAccessTool(checkUnitJob.getAccessTool());

			log.info("Робот успешно завершил работу: "+robotName);
			return message;
		} catch (Exception ex) {
            if(ex instanceof ExecutionException)
                throw ex;
			else
                throw new ExecutionException("Ошибка при выполнении скрипта робота", ex);
		}
	}

	@Override
	public void stop(Long jobId) {
    	log.info("Остановка робота, выполняющего задание {}", jobId);
		try {
			Robot robot = this.robots.get(jobId);
			if(robot != null) {
				robot.destroy();
				this.robots.remove(jobId);
				log.info("Робот, выполняющий задание {} успешно остановлен", jobId);
			} else {
				log.warn("Не найдено активного робота, выполняющего задание " + jobId);
			}
		} catch (Exception ex) {
			log.warn("Ошибка при остановке робота, выполняющего задание " + jobId, ex);
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
		for(Robot robot : robots.values()) {
			try {
				robot.destroy();
			} catch (IOException ex) {
				log.warn("Ошибка при остановке робота", ex);
			}
		}
		this.robots.clear();
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
