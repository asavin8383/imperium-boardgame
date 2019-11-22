package robots;

import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import common.ApplicationConfiguration;
import execution.ExecutionJobResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import robots.exceptions.ExecutionException;
import service.CheckUnitVerificationServiceFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ApplicationConfiguration.class})
@PropertySource("file:config/application.yml")
public class TestJobExecution {

	@Autowired
	private CheckUnitVerificationServiceFactory checkUnitVerificationServiceFactory;

	@Test
	public void test() throws ExecutionException, IOException {
		
		CheckUnitJob checkUnitJob = new CheckUnitJob();
		checkUnitJob.setJobID(1L);
		checkUnitJob.setAccessTool("hola");
		
		checkUnitJob.setCheckUnit(new CheckUnit(1L, CheckUnitType.URL, "http://bet1xbet.ru"));
		//checkUnitJob.setCheckUnit(new CheckUnit(1L, CheckUnitType.IP_V6, "2606:4700:0030:0000:0000:0000:681b:b458"));

		ExecutionJobResult executionJobResult = checkUnitVerificationServiceFactory
				.getService(checkUnitJob.getCheckUnit().getType())
				.run(checkUnitJob);

//		Files.write(Paths.get("output.jpg"), executionJobResult.getScreenshot(),
//			StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		ByteArrayInputStream bis = new ByteArrayInputStream(executionJobResult.getScreenshot());
		BufferedImage bImage2 = ImageIO.read(bis);
		ImageIO.write(bImage2, "png", new File("output.png") );

		System.out.println(executionJobResult.toString());
	}
	
}
