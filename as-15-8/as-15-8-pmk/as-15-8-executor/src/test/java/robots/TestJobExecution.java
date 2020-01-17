package robots;

import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import common.ApplicationConfiguration;
import execution.ExecutionJobResult;
import execution.ExecutionPSJobResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import robots.exceptions.ExecutionException;
import service.CheckUnitVerificationServiceFactory;

import java.io.IOException;

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
		checkUnitJob.setAccessTool("google");

		checkUnitJob.setCheckUnit(new CheckUnit(1L, CheckUnitType.DOMAIN, "telegram.org"));
		//checkUnitJob.setCheckUnit(new CheckUnit(1L, CheckUnitType.IP_V6, "2606:4700:0030:0000:0000:0000:681b:b458"));

		ExecutionJobResult executionJobResult = checkUnitVerificationServiceFactory
				.getService(checkUnitJob)
				.run(checkUnitJob);

//		Files.write(Paths.get("output.jpg"), executionJobResult.getScreenshot(),
//			StandardOpenOption.CREATE, StandardOpenOption.APPEND);
//		ByteArrayInputStream bis = new ByteArrayInputStream(executionJobResult.getScreenshot());
//		BufferedImage bImage2 = ImageIO.read(bis);
//		ImageIO.write(bImage2, "png", new File("output.png") );
//
//        ByteArrayInputStream bis2 = new ByteArrayInputStream(executionJobResult.getEtalonScreenshot());
//        BufferedImage bImage3 = ImageIO.read(bis2);
//        ImageIO.write(bImage3, "png", new File("outputEtalon.png") );

		System.out.println(((ExecutionPSJobResult)executionJobResult).getCheckUnitJobResult());
	}
	
}
