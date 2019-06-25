package robots.factory;

import java.util.Map;

import enums.AccessToolParameters;
import enums.AccessToolUnit;
import robots.Robot;

/**
 * Интерфейс робота проверки ПС/ПАСД
 * @author shabalinAI
 *
 */
public interface RobotsFactory {
	
	/**
	 * Проверяемая ПС/ПАСД
	 * @return
	 */
	AccessToolUnit getAccessToolUnit();
	
	Robot createScript(Map<AccessToolParameters, String> params);
}
