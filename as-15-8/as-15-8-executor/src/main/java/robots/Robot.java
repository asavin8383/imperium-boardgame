package robots;

import org.openqa.selenium.Platform;

import enums.AccessToolUnit;
import lombok.Getter;

public class Robot<T> {

	@Getter
	private AccessToolUnit accessToolUnit;
	
	@Getter
	private Class<T> scriptClass;
	
	@Getter
	private String browserName;
	
	@Getter
	private String platformName;
	
	@Getter
	private String applicationName;
	
	public Robot(AccessToolUnit accessToolUnit, Class<T> scriptClass, String browserName, Platform platform, String applicationName) {
		this.accessToolUnit = accessToolUnit;
		this.scriptClass = scriptClass;
		this.browserName = browserName;
		this.platformName = platform.name();
		this.applicationName = applicationName;
	}

}
