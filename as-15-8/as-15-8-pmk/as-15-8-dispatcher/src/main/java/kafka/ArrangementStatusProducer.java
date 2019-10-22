package kafka;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import arrangement.ArrangementStatusNotification;
import exceptions.AS_15_8_DispatcherException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ArrangementStatusProducer {

	@Autowired
	private KafkaTemplate<String, ArrangementStatusNotification> arrangementStatusKafkaTemplate;
	
	@Value("${spring.kafka.arrangement-notifications-topic}")
	private String arrangementNotificationsTopicName;
	
    public void sendArrangementStatusMessage(ArrangementStatusNotification arrangementStatusNotification) {
    	try {
			Message<ArrangementStatusNotification> message = MessageBuilder
	                .withPayload(arrangementStatusNotification)
	                .setHeader(KafkaHeaders.TOPIC, arrangementNotificationsTopicName)
	                .build();
			
			ListenableFuture<SendResult<String, ArrangementStatusNotification>> future =
					arrangementStatusKafkaTemplate.send(message);
		     
		    future.addCallback(new ListenableFutureCallback<SendResult<String, ArrangementStatusNotification>>() {
		 
		        @Override
		        public void onSuccess(SendResult<String, ArrangementStatusNotification> result) {
		            log.info("Сообщение успешно отправлено: " + result.getProducerRecord().value().toString());
		        }
		        @Override
		        public void onFailure(Throwable ex) {
		        	throw new AS_15_8_DispatcherException("Ошибка при отправке сообщения со статусом мероприятия", ex);
		        }
		    });
		    future.get();
		} catch (Exception ex) {
			throw new AS_15_8_DispatcherException("Ошибка при отправке сообщения со статусом мероприятия", ex);
		}
    }
	
}
