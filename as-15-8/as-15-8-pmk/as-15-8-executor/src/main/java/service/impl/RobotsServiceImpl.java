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
import org.springframework.kafka.listener.AbstractMessageListenerContainer;
import org.springframework.stereotype.Service;
import robots.Robot;
import robots.exceptions.BadIP_ExecutionExeption;
import robots.exceptions.Captcha_ExecutionException;
import robots.exceptions.CloudflareBlockExecutionException;
import robots.exceptions.ExecutionException;
import robots.factory.RobotsFactory;
import service.CheckUnitVerificationService;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Сервис управления роботами Selenium
 *
 * @author shabalinAI
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RobotsServiceImpl implements CheckUnitVerificationService {

    /**
     * Список роботов
     */
    private final RobotsFactory robotsFactory;

    private final ExecutorProperties executorProps;

    private volatile Set<Robot> robots = ConcurrentHashMap.newKeySet();

    private boolean isRunning = false;


    public Map<AccessToolUnit, List<CheckUnitType>> getSupportedTypes() {
        return new HashMap<AccessToolUnit, List<CheckUnitType>>() {{
            put(AccessToolUnit.SEARCH_SYSTEM, Arrays.asList(CheckUnitType.URL, CheckUnitType.DOMAIN, CheckUnitType.SEARCH_PHRASE));
            put(AccessToolUnit.PROXY, Arrays.asList(CheckUnitType.URL, CheckUnitType.DOMAIN));
            put(AccessToolUnit.VPN, Arrays.asList(CheckUnitType.URL, CheckUnitType.DOMAIN));
            put(AccessToolUnit.CAMELEO_XYZ, Collections.singletonList(CheckUnitType.URL));
            put(AccessToolUnit.ANONYMIZER, Collections.singletonList(CheckUnitType.URL));
            put(AccessToolUnit.HIDEMYASS, Collections.singletonList(CheckUnitType.URL));
            put(AccessToolUnit.HOLA, Collections.singletonList(CheckUnitType.URL));
            put(AccessToolUnit.EXTENSION, Collections.singletonList(CheckUnitType.URL));
            put(AccessToolUnit.GOOGLE_API, Arrays.asList(CheckUnitType.URL, CheckUnitType.DOMAIN, CheckUnitType.SEARCH_PHRASE));
            put(AccessToolUnit.PURE_CHANNEL, Collections.singletonList(CheckUnitType.URL));
        }};
    }

    @Override
    public ExecutionJobResult run(Long jobId, CheckUnitJob checkUnitJob) throws ExecutionException {
        try {
            String robotName = String.format("jobId = %s, accessTool = %s, checkUnit = %s",
                    jobId,
                    checkUnitJob.getAccessTool(),
                    checkUnitJob.getCheckUnit().getValue()
            );

            log.info("Запуск робота: {}", robotName);

            Robot robot = robotsFactory.createRobot(checkUnitJob.getAccessTool());
            robot.setRemainingAttempts(executorProps.getExecutor().getMaxRetryAttempts());

            robots.add(robot);

            ExecutionJobResult message;
            boolean needToStop = true;

            try {
                message = runWithRetry(robot, checkUnitJob.getCheckUnit());
            } catch (Exception ex) {
                if (ex instanceof ExecutionException) {
                    if (ex instanceof Captcha_ExecutionException || ex instanceof BadIP_ExecutionExeption || ex instanceof CloudflareBlockExecutionException) {
                        needToStop = false;
                    }
                    throw (ExecutionException) ex;
                } else
                    throw new ExecutionException("Ошибка при выполнении скрипта робота", ex);
            } finally {
                if (needToStop) {
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

            log.info("Робот успешно завершил работу: " + robotName);
            return message;
        } catch (Throwable ex) {
            if (ex instanceof ExecutionException)
                throw ex;
            else
                throw new ExecutionException("Ошибка при выполнении скрипта робота", ex);
        }
    }

    public ExecutionJobResult runWithRetry(Robot robot, CheckUnit checkUnit) throws Exception {
        try {
            robot.setRemainingAttempts(robot.getRemainingAttempts() - 1);
            log.info("Запуск {}-й попытки проверки ресурса: {}, таймаут {}",
                    executorProps.getExecutor().getMaxRetryAttempts() - robot.getRemainingAttempts(),
                    checkUnit.getValue(),
                    executorProps.getExecutor().getTimeout());

            boolean throwExceptionByCaptchaOrBadIP = true;
            if (robot.getRemainingAttempts() == 0) {
                throwExceptionByCaptchaOrBadIP = false;
            }
            return robot.run(checkUnit, executorProps.getExecutor().getTimeout(), throwExceptionByCaptchaOrBadIP);
        } catch (ExecutionException | WebDriverException ex) {
            if (robot.getRemainingAttempts() > 0) {
                log.warn("Будет выполнен {}-й перезапуск проверки ресурса {} по следующей причине: {}",
                        executorProps.getExecutor().getMaxRetryAttempts() - robot.getRemainingAttempts(),
                        checkUnit.getValue(), ex.getMessage());
                try {
                    Thread.sleep(executorProps.getExecutor().getMaxRetryDelay() * 1000);
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
        } catch (Throwable ex) {
            throw new Exception("Ошибка робота", ex);
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
        for (Robot robot : robots) {
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
