package kafka;

import common.ApplicationConfiguration;
import control.ExecutorControlMessage;
import control.ExecutorControlMessage.ControlCommand;
import lombok.RequiredArgsConstructor;
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

@RunWith(SpringRunner.class)
@SpringBootTest(classes={ApplicationConfiguration.class})
@PropertySource("classpath:application.yml")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SendResumeMessage {

    private final KafkaTemplate<String, ExecutorControlMessage> controlMessagesTemplate;
	
	@Value("${spring.kafka.control-topic}")
    private String topic;
	
	@Test
	public void test() {
			
		ExecutorControlMessage controlMessage = new ExecutorControlMessage("google", ControlCommand.START);
		
		Message<ExecutorControlMessage> message = MessageBuilder
                .withPayload(controlMessage)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .build();
		
		ListenableFuture<SendResult<String, ExecutorControlMessage>> future = controlMessagesTemplate.send(message);
	     
	    future.addCallback(new ListenableFutureCallback<SendResult<String, ExecutorControlMessage>>() {
	 
	        @Override
	        public void onSuccess(SendResult<String, ExecutorControlMessage> result) {
	            System.out.println("Сообщение успешно отправлено: "+ result.getProducerRecord().value().toString());
	        }
	        @Override
	        public void onFailure(Throwable ex) {
	            System.out.println("Ошибка при отправке сообщения: " + ex.getMessage());
	        }
	    });
	}
	
}
