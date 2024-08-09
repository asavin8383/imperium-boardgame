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
		checkUnitJob.setAccessTool("pure-channel-pasd");
		String resource = "https://www.ya.ru";
		String resource1 = "https://a-s.bxfilm-4.me";
		String resource2 = "https://medium.com";

		checkUnitJob.setCheckUnit(new CheckUnit(1L, CheckUnitType.URL, resource));
		//checkUnitJob.setCheckUnit(new CheckUnit(1L, CheckUnitType.IP_V6, "2606:4700:0030:0000:0000:0000:681b:b458"));

		ExecutionJobResult executionJobResult = checkUnitVerificationServiceFactory
				.getService(checkUnitJob)
				.run(1L, checkUnitJob);

		Files.write(Paths.get("output1.jpg"), executionJobResult.getScreenshot(),
			StandardOpenOption.CREATE, StandardOpenOption.APPEND);
//
//        ByteArrayInputStream bis2 = new ByteArrayInputStream(executionJobResult.getEtalonScreenshot());
//        BufferedImage bImage3 = ImageIO.read(bis2);
//        ImageIO.write(bImage3, "png", new File("outputEtalon.png") );

		System.out.println(executionJobResult.toString());
	}
	
}
