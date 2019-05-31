package common;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import enums.AccessToolUnit;
import jobs.ArrangementJob;
import jobs.ERDIJob;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={TestKafkaSenderConfig.class})
@PropertySource("classpath:application.yml")
public class SendJobToKafka {

	@Autowired
    private KafkaTemplate<String, ArrangementJob> kafkaTemplate;
	
	@Value("${spring.kafka.arrangement-tasks-topic}")
    private String topic;
	
	@Test
	public void test() {
		
		ArrangementJob job = new ArrangementJob();
		job.setId(100500L);
		job.setAccessToolUnit(AccessToolUnit.YANDEX);
		List<ERDIJob> erdi = new ArrayList<>();
		erdi.add(new ERDIJob(1L));
		job.setErdiJobList(erdi);
		
		Message<ArrangementJob> message = MessageBuilder
                .withPayload(job)
                .setHeader(KafkaHeaders.TOPIC, topic)
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
	
}
