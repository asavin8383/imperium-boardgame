package robots;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import enums.AccessTool;

@Service
public class RobotsFactory {

	@Autowired
	private List<Robot<?>> robots;
	
	private static final Map<AccessTool, Robot<?>> robotsCache = new HashMap<>();
	
	@PostConstruct
	public void initRobotsCache() {
		robots.forEach(robot -> robotsCache.put(robot.getAccessTool(), robot));
	}
	
	public static Robot<?> getRobot(AccessTool accessTool) {
		Robot<?> robot = robotsCache.get(accessTool);
		if(robot == null) {
			throw new IllegalArgumentException("Error creating robot! Robot for " + accessTool + " is not supported");
		}
		return robot;
	}
	
}
