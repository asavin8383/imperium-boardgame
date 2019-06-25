package robots.factory;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import enums.AccessToolUnit;

/**
 * Фабрика роботов проверки ПС/ПАСД
 * @author shabalinAI
 *
 */
@Service
public class RobotsFactoryRegistry {

	/** Список роботов */
	@Autowired
	private List<RobotsFactory> robots;
	
	/** Кэш роботов */
	private static final Map<AccessToolUnit, RobotsFactory> robotsCache = new HashMap<>();
	
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
	public static RobotsFactory getRobot(AccessToolUnit accessToolUnit) {
		RobotsFactory robot = robotsCache.get(accessToolUnit);
		if(robot == null) {
			throw new IllegalArgumentException("Error creating robot! Robot for " + accessToolUnit + " is not supported");
		}
		return robot;
	}
}
