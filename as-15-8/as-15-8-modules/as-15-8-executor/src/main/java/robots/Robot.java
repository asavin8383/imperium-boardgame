package robots;

import java.io.Closeable;

import checkUnits.CheckUnit;
import execution.ExecutionJobResult;
import robots.exceptions.RobotScriptExecutionException;

/**
 * Интефейс скрипта робота
 * @author shabalinAI
 *
 */
public interface Robot extends Closeable {

	/**
	 * Метод выполнения скрипта робота
	 * @param checkUnit
	 * @return
	 * @throws RobotScriptExecutionException
	 */
	ExecutionJobResult run(CheckUnit checkUnit) throws RobotScriptExecutionException;
	
}
