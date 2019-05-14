package robots;

import org.openqa.selenium.Platform;

import enums.AccessTool;
import lombok.Getter;

public class Robot<T> {

	@Getter
	private AccessTool accessTool;
	
	@Getter
	private Class<T> scriptClass;
	
	@Getter
	private String browserName;
	
	@Getter
	private String platformName;
	
	@Getter
	private String applicationName;
	
	public Robot(AccessTool accessTool, Class<T> scriptClass, String browserName, Platform platform, String applicationName) {
		this.accessTool = accessTool;
		this.scriptClass = scriptClass;
		this.browserName = browserName;
		this.platformName = platform.name();
		this.applicationName = applicationName;
	}
}
