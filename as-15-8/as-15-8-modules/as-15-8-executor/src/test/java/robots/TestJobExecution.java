package robots;

import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import enums.AccessToolParameters;
import enums.AccessToolUnit;
import execution.ExecutionJobResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import robots.exceptions.ExecutionException;
import robots.factory.RobotsFactoryConfiguration;
import service.CheckUnitVerificationServiceFactory;
import service.impl.RobotsServiceImpl;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CheckUnitVerificationServiceFactory.class, RobotsServiceImpl.class, RobotsFactoryConfiguration.class})
@PropertySource("file:config/application.yml")
public class TestJobExecution {
	
	@Test
	public void test() throws ExecutionException, IOException {
		
		CheckUnitJob checkUnitJob = new CheckUnitJob();
		checkUnitJob.setJobID(1L);
		checkUnitJob.setAccessToolUnit(AccessToolUnit.GOOGLE);
		
		checkUnitJob.setCheckUnit(new CheckUnit(CheckUnitType.URL, "https://www.google.ru"));

		checkUnitJob.getAccessToolParameters().put(AccessToolParameters.SEARCH_SYSTEM_URL, "https://www.google.ru");
		checkUnitJob.getAccessToolParameters().put(AccessToolParameters.SEARCH_SYSTEM_XPATH_INPUT_FIELD, "//input[@name=\"q\"]");
		checkUnitJob.getAccessToolParameters().put(AccessToolParameters.SEARCH_SYSTEM_XPATH_CAPTCHA, "//form[@id=\"captcha-form\"]");
		checkUnitJob.getAccessToolParameters().put(AccessToolParameters.SEARCH_SYSTEM_XPATH_NEXT_PAGE, "//*[@id=\"pnnext\"]");
		checkUnitJob.getAccessToolParameters().put(AccessToolParameters.SEARCH_SYSTEM_XPATH_ITEM_LINK, "//div[@class=\"g\"]//div[@class=\"r\"]/a[1]");
		checkUnitJob.getAccessToolParameters().put(AccessToolParameters.INPUT_DELAY, "0");
		checkUnitJob.getAccessToolParameters().put(AccessToolParameters.SEARCH_SYSTEM_RESULT_PAGE_TYPE, "pagination");

		ExecutionJobResult executionJobResult = CheckUnitVerificationServiceFactory
				.getService(checkUnitJob.getCheckUnit().getType())
				.run(checkUnitJob);

		ByteArrayInputStream bis = new ByteArrayInputStream(executionJobResult.getScreenshot());
		BufferedImage bImage2 = ImageIO.read(bis);
		ImageIO.write(bImage2, "jpg", new File("output.jpg") );

		System.out.println(executionJobResult.toString());
	}
	
}
