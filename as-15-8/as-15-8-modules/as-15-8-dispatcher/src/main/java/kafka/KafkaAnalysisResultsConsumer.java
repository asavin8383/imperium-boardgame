package kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import analysis.AnalysisResult;
import arrangement.ArrangementStatusNotification;
import enums.ArrangementStatus;
import exceptions.AS_15_8_DispatcherException;
import lombok.extern.slf4j.Slf4j;
import model.ArrangementResult;
import services.CheckUnitJobService;

/**
 * Creation date: 27.05.2019
 * Author: asavin
 * Обработчик результатов анализа
 */

@Service
@Slf4j
public class KafkaAnalysisResultsConsumer {

	@Autowired
	private CheckUnitJobService checkUnitService;
	
	@Autowired
	private KafkaTemplate<String, ArrangementStatusNotification> arrangementStatusKafkaTemplate;
	
	@Value("${spring.kafka.arrangement-notifications-topic}")
	private String arrangementNotificationsTopicName;
	
    @KafkaListener(
    	topics = "${spring.kafka.analysis-results-topic}",
    	containerFactory = "kafkaAnalysisResultListenerContainerFactory"
    )
    public void consumeAnalysisResults(AnalysisResult analysisResult, Acknowledgment ack) {
		log.info("Принято сообщение с анализом результатов проверки: " + analysisResult.toString());
        CompletableFuture.runAsync(() -> {
        	try {       		
        		ArrangementResult jobResult = checkUnitService.processJobResult(analysisResult);
        		log.info("Результаты выполнения проверки успешно обработаны: " + analysisResult.getJobID());
        		
        		ArrangementStatus arrStatus = checkUnitService.checkArrangementStatus(jobResult.getArrangementId());
        		if(arrStatus == ArrangementStatus.FINISHED)
        			sendArrangementStatusMessage(new ArrangementStatusNotification(jobResult.getArrangementId(), arrStatus));
        		
        	} catch (Exception ex) {
        		log.error("Ошибка при обработке задания на проведение мероприятия: " + analysisResult.toString(), ex);
        	}
        	ack.acknowledge();
        });
    }
    
    private void sendArrangementStatusMessage(ArrangementStatusNotification arrangementStatusNotification) {
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
