package robots.factory;

import enums.AccessToolUnit;
import robots.Robot;

/**
 * Интерфейс робота проверки ПС/ПАСД
 * @author shabalinAI
 *
 */
public interface RobotsFactory {
	
	Robot createRobot(String accessTool);
}
