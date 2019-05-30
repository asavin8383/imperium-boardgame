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
import checkUnits.CheckUnitStatusNotification;
import common.AnalysisException;
import enums.CheckUnitJobResult;
import execution.ExecutionJobResult;
import lombok.extern.slf4j.Slf4j;
import service.AnalyzerService;
import service.AnalyzerServiceFactory;

@Service
@Slf4j
public class KafkaConsumer {
	
	@Autowired
	private KafkaTemplate<String, AnalysisResult> analysisResultTemplate;
	
	@Autowired
	private KafkaTemplate<String, CheckUnitStatusNotification> notificationTemplate;
	
    @Value("${spring.kafka.produce-topic}")
    private String analysisResultTopicName;
    
    @Value("${spring.kafka.notification-topic}")
    private String notificationsTopicName;
	
	@KafkaListener(topics = "${spring.kafka.consume-topic}")
    public void consumeExecutionJobMessage(ExecutionJobResult job, Acknowledgment ack) {
		log.info("Принято задание на анализ: " + job.getJobID() + ", "+job.getCheckUnit().getValue());
        CompletableFuture.runAsync(() -> {
        	try {
        		AnalyzerService<? super ExecutionJobResult> service = AnalyzerServiceFactory.getService(job.getClass());
        		AnalysisResult analysisResult = service.analyzeResult(job);
        		sendAnalysisResult(analysisResult);
        		log.info("Анализ результата проверки ПС/ПАСД выполнен успешно : " + job.getJobID() + ", " + job.getCheckUnit().getValue());
        	} catch (Exception ex) {
        		log.error("Ошибка при обработке задания на анализ результатов проверки ПС/ПАСД : " + job.getJobID() + ", " + job.getCheckUnit().getValue(), ex);
        		sendErrorNotification(job.getJobID());
        	}
        	ack.acknowledge();
        });
    }
	
	/**
	 * Метод отправки результата анализа в тему Kafka
	 * @param analysisResult Результат анализа
	 * @throws AnalysisException 
	 */
	private void sendAnalysisResult(AnalysisResult analysisResult) {
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
	private void sendErrorNotification(Long jobID) {
		try {
			CheckUnitStatusNotification notification = new CheckUnitStatusNotification(jobID, CheckUnitJobResult.INTERNAL_ERROR);
			
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
