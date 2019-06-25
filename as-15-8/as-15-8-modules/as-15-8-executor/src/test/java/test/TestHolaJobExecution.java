package test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import common.ApplicationConfiguration;
import enums.AccessToolUnit;
import robots.exceptions.RobotScriptExecutionException;
import service.RobotsService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={ApplicationConfiguration.class})
@ActiveProfiles("dev_asinyavin")
public class TestHolaJobExecution
{

	@Autowired
	private RobotsService service;

	@Test
	public void test() throws RobotScriptExecutionException {

		CheckUnitJob checkUnitJob = new CheckUnitJob();
		checkUnitJob.setJobID(1L);
		checkUnitJob.setAccessToolUnit(AccessToolUnit.HOLA);

		checkUnitJob.setCheckUnit(new CheckUnit(CheckUnitType.URL, "myip.ru"));

		service.run(checkUnitJob);
	}

}
