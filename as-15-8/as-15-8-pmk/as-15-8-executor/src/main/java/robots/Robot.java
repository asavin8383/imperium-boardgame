package robots;

import java.io.IOException;

import checkUnits.CheckUnit;
import enums.CheckUnitJobResult;
import execution.ExecutionJobResult;
import robots.exceptions.ExecutionException;

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
	 * @throws ExecutionException
	 */
	ExecutionJobResult run(CheckUnit checkUnit) throws ExecutionException;

	ExecutionJobResult createMessage(boolean linkFound, CheckUnitJobResult checkUnitJobResult);
	
	/**
	 * Метод уничтожения робота
	 * @throws IOException
	 */
	void destroy() throws IOException;
}
