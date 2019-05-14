package service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.testng.TestNG;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlSuite.ParallelMode;
import org.testng.xml.XmlTest;

import jobs.ArrangementJob;
import jobs.ERDIJob;
import robots.Robot;
import robots.RobotsFactory;
import service.RobotsService;

@Service
public class SeleniumRobotsService implements RobotsService {
	
	public boolean run(ArrangementJob arrangementJob) {
		Robot robot = RobotsFactory.getRobot(arrangementJob.getAccessToolUnit());
		
		TestNG testNG = new TestNG();
		testNG.setUseDefaultListeners(false);
		
		List<XmlSuite> suites = new ArrayList<XmlSuite>(); 
		for(ERDIJob erdiJob : arrangementJob.getErdiJobList()) {
			XmlSuite suite = new XmlSuite(); 
			suite.setName("Arrangement: "+arrangementJob.getId()+", ERDI: "+erdiJob.getId());
			List<XmlTest> tests = new ArrayList<XmlTest>();
			for(String url : erdiJob.getUrls()) {
				XmlTest test = robot.createTest(String.valueOf(tests.size()), arrangementJob.getId(), erdiJob.getId(), url);
				test.setSuite(suite);
				tests.add(test);
			}
			suite.setTests(tests);
			suites.add(suite);
		}
	    
	    testNG.setXmlSuites(suites);
	    testNG.setParallel(ParallelMode.TESTS);
	    testNG.run();
		
		return true;
	}
}
