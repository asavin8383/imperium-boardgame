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

import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import enums.AccessToolUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes={TestKafkaSenderConfig.class})
@PropertySource("classpath:application.yml")
public class SendJobToKafka {

	@Autowired
    private KafkaTemplate<String, CheckUnitJob> kafkaTemplate;
	
	@Value("${spring.kafka.consume-topic}")
    private String topic;
	
	@Test
	public void test() {
		
		CheckUnitJob checkUnitJob = new CheckUnitJob();
		checkUnitJob.setJobID(1L);
		checkUnitJob.setAccessToolUnit(AccessToolUnit.GOOGLE);
		
		checkUnitJob.setCheckUnit(new CheckUnit(CheckUnitType.URL, "https://www.google.ru"));
		
		Message<CheckUnitJob> message = MessageBuilder
                .withPayload(checkUnitJob)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .build();
		
		ListenableFuture<SendResult<String, CheckUnitJob>> future = kafkaTemplate.send(message);
	     
	    future.addCallback(new ListenableFutureCallback<SendResult<String, CheckUnitJob>>() {
	 
	        @Override
	        public void onSuccess(SendResult<String, CheckUnitJob> result) {
	            System.out.println("Сообщение успешно отправлено: arrangenmentID "+ result.getProducerRecord().value().toString());
	        }
	        @Override
	        public void onFailure(Throwable ex) {
	            System.out.println("Ошибка при отправке сообщения: " + ex.getMessage());
	        }
	    });
	}
	
}
