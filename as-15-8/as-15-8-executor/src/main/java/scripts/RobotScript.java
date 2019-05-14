package scripts;

import org.openqa.selenium.WebDriver;

public interface RobotScript {

	void setDriver(WebDriver driver);
	
	void execute();
	
}
