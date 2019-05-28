package kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
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
import common.AnalysisException;
import execution.ExecutionPSJobResult;
import lombok.extern.slf4j.Slf4j;
import service.AnalyzerService;
import service.AnalyzerServiceFactory;

@Service
@Slf4j
public class KafkaConsumer {
	
	@Autowired
	private KafkaTemplate<String, AnalysisResult> analysisResultTemplate;
	
	@Autowired
	private String analysisResultTopicName;
	
	@KafkaListener(topics = "${spring.kafka.consume-topic}")
    public void consumeExecutionPSJobMessage(ExecutionPSJobResult job, Acknowledgment ack) {
		log.info("Принято задание на анализ результата проверки ПС/ПАСД: " + job.getCheckUnit().getValue());
        CompletableFuture.runAsync(() -> {
        	try {
        		AnalyzerService service = AnalyzerServiceFactory.getService(job.getClass());
        		AnalysisResult analysisResult = service.analyzeResult(job);
        		sendAnalysisResult(analysisResult);
        		log.info("Анализ результата проверки ПС/ПАСД выполнен успешно : " + job.getCheckUnit().getValue());
        	} catch (Exception ex) {
        		log.error("Ошибка при обработке задания на анализ результатов проверки ПС/ПАСД : " + job.getCheckUnit().getValue(), ex);
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
		            log.info("Сообщение успешно отправлено: " +
		            		"jobID: " + result.getProducerRecord().value().getJobID() + ", " +
		            		"CheckUnit: " + result.getProducerRecord().value().getCheckUnit().getValue());
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
}
