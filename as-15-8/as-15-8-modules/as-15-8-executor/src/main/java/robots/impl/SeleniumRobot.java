package robots.impl;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.Platform;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlTest;

import checkUnits.CheckUnit;
import enums.AccessToolParameters;
import enums.AccessToolUnit;
import lombok.Getter;
import robots.Robot;

/**
 * Робот на технологии Selenium
 * @author shabalinAI
 *
 * @param <T>
 */
public class SeleniumRobot<T> implements Robot{

	/** Проверяемая ПС/ПАСД */
	@Getter
	private AccessToolUnit accessToolUnit;
	
	/** Класс скрипта робота */
	private Class<T> scriptClass;
	
	/** URL хаба selenium Grid */
	private URL hubURL;
	
	/** Имя браузера */
	private String browserName;
	
	/** Имя платформы */
	private String platformName;
	
	/** Имя приложения (ПС/ПАСД) */
	private String applicationName;


	/**
	 * Робот на технологии Selenium
	 * @param accessToolUnit Проверяемая ПС/ПАСД
	 * @param hubURL Класс скрипта робота
	 * @param scriptClass URL хаба selenium Grid
	 * @param browserName Имя браузера
	 * @param platform Имя платформы
	 * @param applicationName Имя приложения (ПС/ПАСД)
	 */
	public SeleniumRobot(AccessToolUnit accessToolUnit,
			URL hubURL,
			Class<T> scriptClass,
			String browserName, 
			Platform platform,
			String applicationName) {
		this(accessToolUnit, hubURL, scriptClass, browserName, platform, applicationName, null);
	}

	/**
	 * Робот на технологии Selenium
	 * @param accessToolUnit Проверяемая ПС/ПАСД
	 * @param hubURL Класс скрипта робота
	 * @param scriptClass URL хаба selenium Grid
	 * @param browserName Имя браузера
	 * @param platform Имя платформы
	 * @param applicationName Имя приложения (ПС/ПАСД)
	 * @param vpnProxy Прокси по умолчанию (ПАСД)
	 */
	public SeleniumRobot(AccessToolUnit accessToolUnit,
						 URL hubURL,
						 Class<T> scriptClass,
						 String browserName,
						 Platform platform,
						 String applicationName,
						 String vpnProxy) {
		this.accessToolUnit = accessToolUnit;
		this.hubURL = hubURL;
		this.scriptClass = scriptClass;
		this.browserName = browserName;
		this.platformName = platform.name();
		this.applicationName = applicationName;
	}
	
	@Override
	public XmlTest createTest(String name, Long jobID, CheckUnit checkUnit, Map<AccessToolParameters, String> accessToolParameters) {
		XmlTest test = new XmlTest();
		
		test.setName(name);
		
		test.addParameter("hubURL", this.hubURL.toString());
		test.addParameter("browserName", this.browserName);
		test.addParameter("platformName", this.platformName);
		test.addParameter("applicationName", this.applicationName);
		
		test.addParameter("jobID", jobID.toString());
		test.addParameter("checkUnitType", checkUnit.getType().toString());
		test.addParameter("checkUnitValue", checkUnit.getValue());

		List<XmlClass> classes = new ArrayList<XmlClass>();
		classes.add(new XmlClass(this.scriptClass));
		test.setXmlClasses(classes);
		
		return test;
	}
	
}
