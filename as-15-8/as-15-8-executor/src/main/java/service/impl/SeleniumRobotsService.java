package service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.testng.TestNG;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import enums.AccessTool;
import robots.Robot;
import robots.RobotsFactory;
import service.RobotsService;

@Service
public class SeleniumRobotsService implements RobotsService {
	
	@Value("${selenium-hub-url}")
	private String seleniumHubUrl;
	
	public boolean run(AccessTool accessTool) {
		Robot<?> robot = RobotsFactory.getRobot(accessTool);
		
		TestNG testNG = new TestNG();
		testNG.setUseDefaultListeners(false);
		
		XmlSuite suite = new XmlSuite(); 
		suite.setParallel(XmlSuite.ParallelMode.TESTS);
		
		XmlTest test = new XmlTest(suite);
		test.addParameter("hubURL", seleniumHubUrl);
		test.addParameter("browserName", robot.getBrowserName());
		test.addParameter("platformName", robot.getPlatformName());
		test.addParameter("applicationName", robot.getApplicationName());
		
		List<XmlClass> classes = new ArrayList<XmlClass>();
		classes.add(new XmlClass(robot.getScriptClass()));
		test.setXmlClasses(classes);
		
		List<XmlTest> tests = new ArrayList<XmlTest>(); 
		tests.add(test);
	    suite.setTests(tests);
	    
	    List<XmlSuite> suites = new ArrayList<XmlSuite>(); 
	    suites.add(suite);
	    
	    testNG.setXmlSuites(suites);
	    testNG.run();
		
		return true;
	}
	
}
