package robot;

import org.openqa.selenium.WebDriver;

import enums.AccessToolUnit;
import scripts.RobotScript;

public interface Robot {

	AccessToolUnit getAccessTool();
	
	Class<? extends RobotScript> getRobotScriptClass();
	
	boolean run();
	
	WebDriver getDriver();
	
}
