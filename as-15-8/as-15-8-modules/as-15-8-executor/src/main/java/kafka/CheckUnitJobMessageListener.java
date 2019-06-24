package kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;

import checkUnits.CheckUnitJob;
import lombok.extern.slf4j.Slf4j;
import robots.Robot;
import robots.RobotsFactory;
import scripts.RobotScript;

@Slf4j
public class CheckUnitJobMessageListener implements AcknowledgingMessageListener<String, CheckUnitJob> {

	private CheckUnitJobMessageProcessor consumer;
	
	private RobotScript script;
	
	private final String id;
	
	public CheckUnitJobMessageListener(String id, CheckUnitJobMessageProcessor consumer) {
		this.id = id;
		this.consumer = consumer;
	}
	
	@Override
	public void onMessage(ConsumerRecord<String, CheckUnitJob> data, Acknowledgment acknowledgment) {
		Robot robot = RobotsFactory.getRobot(data.value().getAccessToolUnit());
		this.script = robot.createScript(data.value().getAccessToolParameters());
		this.consumer.process(script, data, acknowledgment);
	}
	
	public void destroy() throws Exception {
		if(this.script != null) {
			script.close();
			script = null;
			log.info(id+": script CLOSED");
		} else {
			log.info(id+": script is null");
		}
	}
}

@FunctionalInterface
interface CheckUnitJobMessageProcessor {
	
	void process(RobotScript script, ConsumerRecord<String, CheckUnitJob> data, Acknowledgment acknowledgment);
	
}
