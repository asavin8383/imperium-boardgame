package common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import enums.AccessToolUnit;
import jobs.ArrangementJob;
import jobs.ERDIJob;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={SendJobToKafka.TestConfig.class})
@PropertySource("classpath:application.yml")
public class SendJobToKafka {

	@Autowired
    private KafkaTemplate<String, ArrangementJob> kafkaTemplate;
	
	@Value("${spring.cloud.stream.bindings.incomingArrangements.destination}")
    private String topic;
	
	@Test
	public void test() {
		
		ArrangementJob job = new ArrangementJob();
		job.setId(100500L);
		job.setAccessToolUnit(AccessToolUnit.SEARCH_SYSTEM);
		List<ERDIJob> erdi = new ArrayList<>();
		erdi.add(new ERDIJob(169111L));
		job.setErdiJobList(erdi);
		
		Message<ArrangementJob> message = MessageBuilder
                .withPayload(job)
                .setHeader(KafkaHeaders.TOPIC, topic)
				.setHeader(MessageHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_JSON)
                .build();
		
		ListenableFuture<SendResult<String, ArrangementJob>> future = kafkaTemplate.send(message);
	     
	    future.addCallback(new ListenableFutureCallback<SendResult<String, ArrangementJob>>() {
	 
	        @Override
	        public void onSuccess(SendResult<String, ArrangementJob> result) {
	            System.out.println("Сообщение успешно отправлено: arrangenmentID "+ result.getProducerRecord().value().toString());
	        }
	        @Override
	        public void onFailure(Throwable ex) {
	            System.out.println("Ошибка при отправке сообщения: " + ex.getMessage());
	        }
	    });
	}

	@Configuration
	static class TestConfig {

		@Value("${spring.cloud.stream.kafka.binder.brokers}")
		private String bootstrapServers;

		@Bean
		public ProducerFactory<String, ArrangementJob> producerFactory() {
			Map<String, Object> configProps = new HashMap<>();
			configProps.put(
					ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
					bootstrapServers);
			configProps.put(
					ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
					StringSerializer.class);
			configProps.put(
					ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
					JsonSerializer.class);
			return new DefaultKafkaProducerFactory<>(configProps);
		}

		@Bean
		public KafkaTemplate<String, ArrangementJob> kafkaTemplate() {
			return new KafkaTemplate<>(producerFactory());
		}
	}
	
}
