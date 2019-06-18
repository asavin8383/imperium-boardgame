package scripts;

import java.io.Closeable;

import checkUnits.CheckUnit;
import execution.ExecutionJobResult;
import scripts.exceptions.RobotScriptExecutionException;

/**
 * Интефейс скрипта робота
 * @author shabalinAI
 *
 */
public interface RobotScript extends Closeable {

	/**
	 * Метод выполнения скрипта робота
	 * @param checkUnit
	 * @return
	 * @throws RobotScriptExecutionException
	 */
	ExecutionJobResult execute(CheckUnit checkUnit) throws RobotScriptExecutionException;
	
}
