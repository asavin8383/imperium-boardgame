package integration;

import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.ApplicationConfiguration;
import enums.AccessToolParameters;
import enums.AccessToolUnit;
import events.ExecutorChannels;
import execution.ExecutionJobResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.context.annotation.PropertySource;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Objects;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ApplicationConfiguration.class})
@PropertySource("file:config/application.yml")
public class IntegrationTests {

	@Autowired
	private ExecutorChannels executorChannels;

	@Autowired
	private MessageCollector messageCollector;

	@Test
	public void testPSRobot() throws IOException {

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

		Message<CheckUnitJob> message = MessageBuilder
				.withPayload(checkUnitJob)
				.build();

		sendMessage(message);
		checkOutputMessage();
	}

	@Test
	public void testNMap() throws IOException {

		CheckUnitJob checkUnitJob = new CheckUnitJob();
		checkUnitJob.setAccessToolUnit(AccessToolUnit.KASPERSKY);
		checkUnitJob.setJobID(1L);
		checkUnitJob.setCheckUnit(new CheckUnit(CheckUnitType.IP_V4, "192.167.1.1"));

		Message<CheckUnitJob> message = MessageBuilder
				.withPayload(checkUnitJob)
				.build();

		sendMessage(message);
		checkOutputMessage();
	}

	private void sendMessage(Message message){
		assertTrue(executorChannels
				.jobs()
				.send(message));
	}

	private void checkOutputMessage() throws IOException {
		if(!messageCollector.forChannel(executorChannels.executionResults()).isEmpty()) {
			String stringResult = Objects.requireNonNull(messageCollector
					.forChannel(executorChannels.executionResults())
					.poll())
					.getPayload()
					.toString();
			ExecutionJobResult executionJobResult = new ObjectMapper()
					.readValue(stringResult, ExecutionJobResult.class);
			System.out.println(executionJobResult);
			assertTrue(true);
		} else if(!messageCollector.forChannel(executorChannels.notifications()).isEmpty()) {
			String stringResult = Objects.requireNonNull(messageCollector
					.forChannel(executorChannels.notifications())
					.poll())
					.getPayload()
					.toString();
			System.out.println(stringResult);
			fail();
		}
	}
}
