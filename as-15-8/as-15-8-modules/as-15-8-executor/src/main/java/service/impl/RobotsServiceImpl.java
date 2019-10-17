package service.impl;

import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import enums.AccessToolUnit;
import execution.ExecutionJobResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.stereotype.Service;
import robots.Robot;
import robots.exceptions.Cancel_ExecutionException;
import robots.exceptions.Captcha_ExecutionException;
import robots.exceptions.ExecutionException;
import robots.factory.RobotsFactory;
import service.CheckUnitVerificationService;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	
    private volatile Set<Robot> robots = new HashSet<>();
    
    private boolean isRunning = false;

	@Override
	public List<CheckUnitType> getCheckUnitTypes(){
		return Arrays.asList(CheckUnitType.URL);
	}

	@Override
	public ExecutionJobResult run(CheckUnitJob checkUnitJob) throws ExecutionException {
		try {
			String robotName = "jobID = " + checkUnitJob.getJobID() +
                    " accessTool = " + checkUnitJob.getAccessTool() +
                    " checkUnit = " + checkUnitJob.getCheckUnit().getValue();
			if(!this.isRunning)
				throw new ExecutionException("Ошибка при запуске проверки запрещенного ресурса. Сервис проверки остановлен!");
			log.info("Запуск робота: " + robotName);

			//TODO Добавить имя робота
			Robot robot = robotsFactory.createRobot(checkUnitJob.getAccessTool(), "");
			
			robots.add(robot);
			
			ExecutionJobResult message;
			boolean needToStop = true;
			try{
				message = robot.run(checkUnitJob.getCheckUnit());
			} catch (Exception ex) {
				if(ex instanceof ExecutionException) {
					if(ex instanceof Captcha_ExecutionException || ex instanceof Cancel_ExecutionException)
						needToStop = false;
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
			message.setJobID(checkUnitJob.getJobID());
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
