package robots;


import enums.AccessToolUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Фабрика роботов проверки ПС/ПАСД
 * @author shabalinAI
 *
 */
@Service
public class RobotsFactory {

	/** Список роботов */
	@Autowired
	private List<Robot> robots;
	
	/** Кэш роботов */
	private static final Map<AccessToolUnit, Robot> robotsCache = new HashMap<>();
	
	/**
	 * Метод создания кэша роботов 
	 */
	@PostConstruct
	public void initRobotsCache() {
		robots.forEach(robot -> robotsCache.put(robot.getAccessToolUnit(), robot));
	}
	
	/**
	 * Метод получения робота для ПС/ПАСД
	 * @param accessToolUnit ПС/ПАСД
	 * @return
	 */
	public static Robot getRobot(AccessToolUnit accessToolUnit) {
		Robot robot = robotsCache.get(accessToolUnit);
		if(robot == null) {
			throw new IllegalArgumentException("Error creating robot! Robot for " + accessToolUnit + " is not supported");
		}
		return robot;
	}
	
}
