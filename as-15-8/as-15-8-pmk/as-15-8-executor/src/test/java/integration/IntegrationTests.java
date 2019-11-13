package integration;

import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import com.fasterxml.jackson.databind.ObjectMapper;
import common.ApplicationConfiguration;
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
		checkUnitJob.setAccessTool("GOOGLE");

		checkUnitJob.setCheckUnit(new CheckUnit(1L, CheckUnitType.URL, "https://www.google.ru"));

		Message<CheckUnitJob> message = MessageBuilder
				.withPayload(checkUnitJob)
				.build();

		sendMessage(message);
		checkOutputMessage();
	}

	@Test
	public void testNMap() throws IOException {

		CheckUnitJob checkUnitJob = new CheckUnitJob();
		checkUnitJob.setAccessTool("vpn");
		checkUnitJob.setJobID(1L);
		checkUnitJob.setCheckUnit(new CheckUnit(1L, CheckUnitType.IP_V4, "192.167.1.1"));

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
