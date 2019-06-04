package service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.testng.ITestNGListener;
import org.testng.TestNG;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.XmlTest;

import checkUnits.CheckUnitJob;
import listener.RobotListener;
import robots.Robot;
import robots.RobotsFactory;
import service.RobotsService;

/**
 * Сервис управления роботами Selenium
 * @author shabalinAI
 *
 */
@Service
public class SeleniumRobotsService implements RobotsService {
	
	public boolean run(CheckUnitJob checkUnitJob) {
		Robot robot = RobotsFactory.getRobot(checkUnitJob.getAccessToolUnit());
		
		TestNG testNG = new TestNG();
		testNG.setUseDefaultListeners(false);
		
		List<XmlSuite> suites = new ArrayList<XmlSuite>(); 
		XmlSuite suite = new XmlSuite(); 
		suite.setName("JobID: "+checkUnitJob.getJobID());
		suite.setVerbose(0);
		
		List<XmlTest> tests = new ArrayList<XmlTest>();
		XmlTest test = robot.createTest(
			"Job: "+checkUnitJob.getJobID()+", Check: "+checkUnitJob.getCheckUnit().getValue(),
			checkUnitJob.getJobID(),
			checkUnitJob.getCheckUnit(),
			checkUnitJob.getAccessToolParameters()
		);
		
		test.setSuite(suite);
		tests.add(test);
		
		suite.setTests(tests);
		suites.add(suite);
	    
	    testNG.setXmlSuites(suites);
	    testNG.setParallel(ParallelMode.INSTANCES);
	    
	    List<Class<? extends ITestNGListener>> listeners = new ArrayList<>();
	    listeners.add(RobotListener.class);
	    testNG.setListenerClasses(listeners);
	    
	    testNG.run();
		
		return true;
	}
}
