package robots;

import java.util.Map;

import enums.AccessToolParameters;
import enums.AccessToolUnit;
import scripts.RobotScript;

/**
 * Интерфейс робота проверки ПС/ПАСД
 * @author shabalinAI
 *
 */
public interface Robot {
	
	/**
	 * Проверяемая ПС/ПАСД
	 * @return
	 */
	AccessToolUnit getAccessToolUnit();
	
	RobotScript createScript(Map<AccessToolParameters, String> params);
}
