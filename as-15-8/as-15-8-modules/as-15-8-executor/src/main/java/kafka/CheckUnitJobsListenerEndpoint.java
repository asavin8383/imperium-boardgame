package kafka;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.config.KafkaListenerEndpoint;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.listener.adapter.FilteringMessageListenerAdapter;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.TopicPartitionInitialOffset;
import org.springframework.kafka.support.converter.MessageConverter;

import checkUnits.CheckUnitJob;
import enums.AccessToolUnit;

public class CheckUnitJobsListenerEndpoint implements KafkaListenerEndpoint {

	private static final String GROUP_ID_PREFIX = "exec_";
	
	private String id;
	
	private List<String> topics = new ArrayList<>();
	
	private String groupID;
	
	private AccessToolUnit accessToolUnit;
	
	private CheckUnitJobMessageProcessor processor;
	
	CheckUnitJobsListenerEndpoint(String topic, AccessToolUnit accessToolUnit, CheckUnitJobMessageProcessor processor) {		
		this.accessToolUnit = accessToolUnit;
		this.id = accessToolUnit.name();
		this.groupID = GROUP_ID_PREFIX+accessToolUnit.name().toLowerCase();
		this.topics.add(topic);
		this.processor = processor;
	}

	@Override
	public String getId() {
		return id;
	}

	@Override
	public String getGroupId() {
		return groupID;
	}

	@Override
	public Boolean getAutoStartup() {
		return true;
	}
	
	@Override
	public void setupListenerContainer(MessageListenerContainer listenerContainer, MessageConverter messageConverter) {
		listenerContainer.setupMessageListener(new FilteringMessageListenerAdapter<String, CheckUnitJob>(
				(AcknowledgingMessageListener<String, CheckUnitJob>)(data, ack) -> this.processor.process(data, ack), 
    			record -> !record.value().getAccessToolUnit().equals(this.accessToolUnit),
    			true
    		)
		);
	}

	@Override
	public String getGroup() {
		return null;
	}

	@Override
	public Collection<String> getTopics() {
		return topics;
	}

	@Override
	public Collection<TopicPartitionInitialOffset> getTopicPartitions() {
		return Collections.emptyList();
	}

	@Override
	public Pattern getTopicPattern() {
		return null;
	}

	@Override
	public String getClientIdPrefix() {
		return null;
	}

	@Override
	public Integer getConcurrency() {
		return null;
	}
	
	@FunctionalInterface
	interface CheckUnitJobMessageProcessor {
		
		void process(ConsumerRecord<String, CheckUnitJob> data, Acknowledgment acknowledgment);
		
	}
}
