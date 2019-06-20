package robots;

import java.util.Map;

import org.springframework.beans.factory.DisposableBean;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import enums.AccessToolUnit;
import execution.ExecutionJobResult;
import scripts.exceptions.RobotScriptExecutionException;

/**
 * Интерфейс робота проверки ПС/ПАСД
 * @author shabalinAI
 *
 */
public interface Robot extends DisposableBean{
	
	/**
	 * Проверяемая ПС/ПАСД
	 * @return
	 */
	AccessToolUnit getAccessToolUnit();
	
	ExecutionJobResult run(CheckUnit checkUnit, Map<AccessToolParameters, String> accessToolParameters) throws RobotScriptExecutionException;
	
}
