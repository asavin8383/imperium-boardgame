package robots;

import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import common.ApplicationConfiguration;
import execution.ExecutionJobResult;
import lombok.RequiredArgsConstructor;
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

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ApplicationConfiguration.class})
@PropertySource("file:config/application.yml")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TestJobExecution {

	private final CheckUnitVerificationServiceFactory checkUnitVerificationServiceFactory;

	@Test
	public void test() throws ExecutionException, IOException {
		
		CheckUnitJob checkUnitJob = new CheckUnitJob();
		checkUnitJob.setJobID(1L);
		checkUnitJob.setAccessTool("kaspersky");
		
		checkUnitJob.setCheckUnit(new CheckUnit(1L, CheckUnitType.URL, "Http://cannabay.org"));

		ExecutionJobResult executionJobResult = checkUnitVerificationServiceFactory
				.getService(checkUnitJob.getCheckUnit().getType())
				.run(checkUnitJob);

//		Files.write(Paths.get("output.jpg"), executionJobResult.getScreenshot(),
//		StandardOpenOption.CREATE, StandardOpenOption.APPEND);
		ByteArrayInputStream bis = new ByteArrayInputStream(executionJobResult.getScreenshot());
		BufferedImage bImage2 = ImageIO.read(bis);
		ImageIO.write(bImage2, "jpg", new File("output.jpg") );

		System.out.println(executionJobResult.toString());
	}
	
}
