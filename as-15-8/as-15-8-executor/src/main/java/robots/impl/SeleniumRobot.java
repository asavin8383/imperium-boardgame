package robots.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.Platform;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;

import enums.AccessToolUnit;
import lombok.Getter;
import robots.Robot;

public class SeleniumRobot<T> implements Robot{

	@Getter
	private AccessToolUnit accessToolUnit;
	
	private Class<T> scriptClass;
	
	private URL hubURL;
	
	private String browserName;
	
	private String platformName;
	
	private String applicationName;
	
	public SeleniumRobot(AccessToolUnit accessToolUnit,
			URL hubURL,
			Class<T> scriptClass,
			String browserName, 
			Platform platform,
			String applicationName) {
		this.accessToolUnit = accessToolUnit;
		this.hubURL = hubURL;
		this.scriptClass = scriptClass;
		this.browserName = browserName;
		this.platformName = platform.name();
		this.applicationName = applicationName;
	}
	
	@Override
	public XmlTest createTest(String name, Long arrangenmentID, Long erdiID, String url) {
		XmlTest test = new XmlTest();
		
		test.setName(name);
		
		test.addParameter("hubURL", this.hubURL.toString());
		test.addParameter("browserName", this.browserName);
		test.addParameter("platformName", this.platformName);
		test.addParameter("applicationName", this.applicationName);
		
		test.addParameter("arrangenmentID", arrangenmentID.toString());
		test.addParameter("erdiID", erdiID.toString());
		test.addParameter("url", url);
		
		List<XmlClass> classes = new ArrayList<XmlClass>();
		classes.add(new XmlClass(this.scriptClass));
		test.setXmlClasses(classes);
		
		return test;
	}
	
}
