package robots;

import checkUnits.CheckUnit;
import execution.ExecutionJobResult;
import robots.exceptions.ExecutionException;

import java.io.IOException;

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
	ExecutionJobResult run(CheckUnit checkUnit, boolean throwExceptionByCaptchaOrBadIP) throws ExecutionException;

	/**
	 * Метод уничтожения робота
	 * @throws IOException
	 */
	void destroy() throws IOException;

	int getRemainingAttempts();

	void setRemainingAttempts(int value);
}
