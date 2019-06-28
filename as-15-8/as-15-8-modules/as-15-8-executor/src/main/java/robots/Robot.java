package robots;

import java.io.IOException;

import checkUnits.CheckUnit;
import execution.ExecutionJobResult;
import robots.exceptions.RobotScriptExecutionException;

/**
 * Интефейс скрипта робота
 * @author shabalinAI
 *
 */
public interface Robot {

	/**
	 * Метод выполнения скрипта робота
	 * @param checkUnit
	 * @return
	 * @throws RobotScriptExecutionException
	 */
	ExecutionJobResult run(CheckUnit checkUnit) throws RobotScriptExecutionException;
	
	/**
	 * Метод уничтожения робота
	 * @throws IOException
	 */
	void destroy() throws IOException;
}
