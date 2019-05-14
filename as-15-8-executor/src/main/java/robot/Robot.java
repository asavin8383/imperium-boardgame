package robot;

import org.openqa.selenium.WebDriver;

import enums.AccessTool;
import scripts.RobotScript;

public interface Robot {

	AccessTool getAccessTool();
	
	Class<? extends RobotScript> getRobotScriptClass();
	
	boolean run();
	
	WebDriver getDriver();
	
}
