package service.impl;

import org.springframework.stereotype.Service;
import org.testng.TestNG;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import enums.AccessToolUnit;
import robot.Robot;
import robot.RobotsFactory;
import service.RobotsService;

@Service
public class SeleniumRobotsService implements RobotsService {
	
	public boolean run(AccessToolUnit accessToolUnit) {
		Robot robot = RobotsFactory.getRobot(accessToolUnit);
		/*return CompletableFuture.supplyAsync(() -> robot.run()).join();*/
		
		TestNG testNG = new TestNG();
		XmlSuite suite = new XmlSuite(); 
		suite.setParallel(XmlSuite.ParallelMode.TESTS);
		XmlTest test = new XmlTest(suite);
		//test.setParameters(parameters);
		return true;
	}
	
}
