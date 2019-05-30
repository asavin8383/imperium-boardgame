package listener;

import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import kafka.JobNotificationsProducer;
import lombok.extern.slf4j.Slf4j;
import scripts.RobotScript;

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
		log.error("Робот завершил работу с ошибкой: "+getTestName(result), result.getThrowable());
	}

	@Override
	public void onTestSkipped(ITestResult result) {
		sendErrorNotification(result);
		log.error("Робот был остановлен: "+getTestName(result), result.getThrowable());
	}

	@Override
	public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
		sendErrorNotification(result);
		log.error("Робот завершил работу с ошибкой: "+getTestName(result), result.getThrowable());
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
		Long jobID = Long.parseLong(((RobotScript)result.getInstance()).getJobID());
		JobNotificationsProducer.getInstance().sendCheckJobErrorNotification(jobID, result.getThrowable());
	}
}
