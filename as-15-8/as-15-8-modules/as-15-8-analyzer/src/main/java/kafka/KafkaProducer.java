package kafka;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import analysis.AnalysisResult;
import checkUnits.CheckUnitStatusNotification;
import common.AnalysisException;
import enums.CheckUnitJobResult;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class KafkaProducer {
	
	private KafkaTemplate<String, AnalysisResult> analysisResultTemplate;
	
	private KafkaTemplate<String, CheckUnitStatusNotification> notificationTemplate;
	
    @Value("${spring.kafka.produce-topic}")
    private String analysisResultTopicName;
    
    @Value("${spring.kafka.notification-topic}")
    private String notificationsTopicName;

	@Autowired
	public KafkaProducer(
			KafkaTemplate<String, AnalysisResult> analysisResultTemplate,
			KafkaTemplate<String, CheckUnitStatusNotification> notificationTemplate) {
		
		this.analysisResultTemplate = analysisResultTemplate;
		this.notificationTemplate = notificationTemplate;
	}
	
	/**
	 * Метод отправки результата анализа в тему Kafka
	 * @param analysisResult Результат анализа
	 * @throws AnalysisException 
	 */
	public void sendAnalysisResult(AnalysisResult analysisResult) {
		try {
			Message<AnalysisResult> message = MessageBuilder
	                .withPayload(analysisResult)
	                .setHeader(KafkaHeaders.TOPIC, analysisResultTopicName)
	                .build();
			
			ListenableFuture<SendResult<String, AnalysisResult>> future = analysisResultTemplate.send(message);
		     
		    future.addCallback(new ListenableFutureCallback<SendResult<String, AnalysisResult>>() {
		 
		        @Override
		        public void onSuccess(SendResult<String, AnalysisResult> result) {
		        	AnalysisResult mess = result.getProducerRecord().value();
		            log.info("Сообщение успешно отправлено: " + mess.getJobID() + ", " + mess.getCheckUnit().getValue());
		        }
		        @Override
		        public void onFailure(Throwable ex) {
		        	throw new AnalysisException("Ошибка при отправке сообщения с результатами анализа", ex);
		        }
		    });
		    future.get();
		} catch (Exception ex) {
			throw new AnalysisException("Ошибка при отправке сообщения с результатами анализа", ex);
		}
	}
	
	/**
	 * Метод отправки результата анализа в тему Kafka
	 * @param analysisResult Результат анализа
	 * @throws AnalysisException 
	 */
	public void sendErrorNotification(Long jobID, Throwable cause) {
		try {
			StringWriter sw = new StringWriter();
			cause.printStackTrace(new PrintWriter(sw));
			CheckUnitStatusNotification notification = new CheckUnitStatusNotification(jobID, CheckUnitJobResult.INTERNAL_ERROR, sw.toString());
			
			Message<CheckUnitStatusNotification> message = MessageBuilder
	                .withPayload(notification)
	                .setHeader(KafkaHeaders.TOPIC, notificationsTopicName)
	                .build();
			
			ListenableFuture<SendResult<String, CheckUnitStatusNotification>> future = notificationTemplate.send(message);
		     
		    future.addCallback(new ListenableFutureCallback<SendResult<String, CheckUnitStatusNotification>>() {
		 
		        @Override
		        public void onSuccess(SendResult<String, CheckUnitStatusNotification> result) {
		        	CheckUnitStatusNotification mess = result.getProducerRecord().value();
		            log.info("Сообщение успешно отправлено: " + mess.getJobID() + ", " + mess.getCheckUnitStatus());
		        }
		        @Override
		        public void onFailure(Throwable ex) {
		        	throw new AnalysisException("Ошибка при отправке сообщения с уведомлением об ошибке", ex);
		        }
		    });
		    future.get();
		} catch (Exception ex) {
			throw new AnalysisException("Ошибка при отправке сообщения с уведомлением об ошибке", ex);
		}
	}
	
}
