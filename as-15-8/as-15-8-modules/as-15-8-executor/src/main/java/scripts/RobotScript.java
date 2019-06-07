package scripts;

import java.io.Closeable;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;

import org.openqa.selenium.WebDriver;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import execution.ExecutionJobResult;
import lombok.Getter;
import scripts.exceptions.RobotScriptExecutionException;

/**
 * Скрипт робота проверки ПС/ПАСД
 * @author shabalinAI
 *
 */
public abstract class RobotScript implements Closeable{

	protected WebDriver driver;
	
	@Getter
	private ScriptDriverParameters driverParams;
	
	@Getter
	private Map<AccessToolParameters, String> scriptParams;

	public RobotScript(ScriptDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams) throws MalformedURLException {
		setParams(driverParams, scriptParams);
		this.driver = DriverFactory.createDriver(
			driverParams.getHubURL(),
			driverParams.getPlatformName(),
			driverParams.getApplicationName(),
			driverParams.getBrowserName()
		);
	}

	public RobotScript(ScriptDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams, String proxy) throws MalformedURLException {
		setParams(driverParams, scriptParams);
		this.driver = DriverFactory.createDriver(
			driverParams.getHubURL(),
			driverParams.getPlatformName(),
			driverParams.getApplicationName(),
			driverParams.getBrowserName(),
			proxy
		);
	}
	
	private void setParams(ScriptDriverParameters driverParams, Map<AccessToolParameters, String> scriptParams) {
		this.driverParams = driverParams;
		this.scriptParams = scriptParams;
	}
	
	/**
	 * Метод, запускаюший выполнение скрипта робота
	 * @param driver Драйвер
	 * @param checkUnit Ресурс для проверки
	 * @return
	 * @throws RobotScriptExecutionException
	 */
	public abstract ExecutionJobResult execute(CheckUnit checkUnit) throws RobotScriptExecutionException;
	
	@Override
	public void close() throws IOException {
		if(this.driver != null)
			driver.quit();
	}
}
