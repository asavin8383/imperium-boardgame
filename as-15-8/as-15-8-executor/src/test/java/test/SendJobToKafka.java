package test;

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
import jobs.CheckUnit;
import jobs.CheckUnitType;
import jobs.ERDIJob;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={TestKafkaSenderConfig.class})
@PropertySource("classpath:application.yml")
public class SendJobToKafka {

	@Autowired
    private KafkaTemplate<String, ArrangementJob> kafkaTemplate;
	
	@Value("${spring.kafka.consume-topic}")
    private String topic;
	
	@Test
	public void test() {
		
		ArrangementJob arrangementJob = new ArrangementJob();
		arrangementJob.setId(1L);
		arrangementJob.setAccessToolUnit(AccessToolUnit.GOOGLE);
		
		for(long i = 0; i < 1; i++) {
			ERDIJob erdiJob = new ERDIJob();
			erdiJob.setId(i);
			for(int j = 0; j < 1; j++)
				erdiJob.addCheckUnit(new CheckUnit(CheckUnitType.URL, "https://www.google.ru"));
			arrangementJob.addERDIJob(erdiJob);
		}
		
		Message<ArrangementJob> message = MessageBuilder
                .withPayload(arrangementJob)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .build();
		
		ListenableFuture<SendResult<String, ArrangementJob>> future = kafkaTemplate.send(message);
	     
	    future.addCallback(new ListenableFutureCallback<SendResult<String, ArrangementJob>>() {
	 
	        @Override
	        public void onSuccess(SendResult<String, ArrangementJob> result) {
	            System.out.println("Сообщение успешно отправлено: arrangenmentID "+ result.getProducerRecord().value().getId());
	        }
	        @Override
	        public void onFailure(Throwable ex) {
	            System.out.println("Ошибка при отправке сообщения: " + ex.getMessage());
	        }
	    });
	}
	
}
