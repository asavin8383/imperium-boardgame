package test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import common.ApplicationConfiguration;
import enums.AccessToolUnit;
import jobs.ArrangementJob;
import jobs.CheckUnit;
import jobs.CheckUnitType;
import jobs.ERDIJob;
import service.impl.SeleniumRobotsService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={ApplicationConfiguration.class})
public class TestJobExecution {

	@Autowired
	private SeleniumRobotsService service;
	
	@Test
	public void test() {
		
		ArrangementJob arrangementJob = new ArrangementJob();
		arrangementJob.setId(1L);
		arrangementJob.setAccessToolUnit(AccessToolUnit.GOOGLE);
		
		for(long i = 0; i < 10; i++) {
			ERDIJob erdiJob = new ERDIJob();
			erdiJob.setId(i);
			for(int j = 0; j < 10; j++)
				erdiJob.addCheckUnit(new CheckUnit(CheckUnitType.URL, "https://www.google.ru"));
			arrangementJob.addERDIJob(erdiJob);
		}
		
		assertTrue(service.run(arrangementJob));
	}
	
}
