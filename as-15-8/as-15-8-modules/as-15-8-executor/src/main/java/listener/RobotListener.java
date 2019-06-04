package listener;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import kafka.JobNotificationsProducer;
import lombok.extern.slf4j.Slf4j;
import scripts.RobotScript;
import scripts.exceptions.Captcha_RobotScriptExecutionException;

/**
 * Обраьотчик результата выполнения роботов
 * @author shabalinAI
 *
 */
@Slf4j
public class RobotListener implements ITestListener{
	
	@Override
	public void onTestStart(ITestResult result) {
		log.info("Робот запущен: "+getTestName(result));
	}

	@Override
	public void onTestSuccess(ITestResult result) {
		log.info("Робот успешно завершил работу: "+getTestName(result));
	}

	@Override
	public void onTestFailure(ITestResult result) {
		sendErrorNotification(result);
		logError(result, false);
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		sendErrorNotification(result);
		logError(result, true);
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
		sendErrorNotification(result);
		logError(result, false);
	}

	@Override
	public void onStart(ITestContext context) {
	}

	@Override
	public void onFinish(ITestContext context) {
	}

	private String getTestName(ITestResult result) {
		return result.getTestClass().getXmlTest().getName();
	}
	
	private void sendErrorNotification(ITestResult result) {
		RobotScript script = (RobotScript)result.getInstance();
		Long jobID = Long.parseLong(script.getJobID());
		JobNotificationsProducer.getInstance().sendCheckJobErrorNotification(jobID, result.getThrowable());
		if(result.getThrowable() instanceof Captcha_RobotScriptExecutionException)
			JobNotificationsProducer.getInstance().sendStopExecutorsMessage(script.getAccessToolUnit());
	}
	
	private void logError(ITestResult result, boolean isSkipped) {
		Throwable ex = result.getThrowable();
		if(ex instanceof Captcha_RobotScriptExecutionException) {
			log.warn("Робот остановлен, обнаружена капча: " + getTestName(result) + ", checkUnit: " +((RobotScript)result.getInstance()).getCheckUnit().getValue());
		} else {
			log.error("Робот "+(isSkipped ? "остановлен" : "завершил работу с ошибкой") + ": "+getTestName(result), ex);
		}
	}
}
