package robots;

import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import execution.ExecutionJobResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import robots.exceptions.ExecutionException;
import robots.factory.RobotsFactory;
import service.CheckUnitVerificationServiceFactory;
import service.impl.RobotsServiceImpl;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CheckUnitVerificationServiceFactory.class, RobotsServiceImpl.class, RobotsFactory.class})
@PropertySource("file:config/application.yml")
public class TestHolaJobExecution
{

	@Test
	public void test() throws ExecutionException, IOException {

		CheckUnitJob checkUnitJob = new CheckUnitJob();
		checkUnitJob.setJobID(1L);
		checkUnitJob.setAccessTool("hola");

		checkUnitJob.setCheckUnit(new CheckUnit(CheckUnitType.URL, "myip.ru"));

		ExecutionJobResult executionJobResult = CheckUnitVerificationServiceFactory
				.getService(checkUnitJob.getCheckUnit().getType())
				.run(checkUnitJob);

		ByteArrayInputStream bis = new ByteArrayInputStream(executionJobResult.getScreenshot());
		BufferedImage bImage2 = ImageIO.read(bis);
		ImageIO.write(bImage2, "jpg", new File("output.jpg"));

		System.out.println(executionJobResult.toString());
	}

}
