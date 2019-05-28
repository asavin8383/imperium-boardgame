package test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import common.ApplicationConfiguration;
import enums.AccessToolUnit;
import service.impl.SeleniumRobotsService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={ApplicationConfiguration.class})
public class TestJobExecution {

	@Autowired
	private SeleniumRobotsService service;
	
	@Test
	public void test() {
		
		CheckUnitJob checkUnitJob = new CheckUnitJob();
		checkUnitJob.setJobID(1L);
		checkUnitJob.setAccessToolUnit(AccessToolUnit.GOOGLE);
		
		checkUnitJob.setCheckUnit(new CheckUnit(CheckUnitType.URL, "https://www.google.ru"));
		
		assertTrue(service.run(checkUnitJob));
	}
	
}
