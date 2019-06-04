package kafka.listeners;

import java.util.concurrent.CompletableFuture;

import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.listener.AcknowledgingConsumerAwareMessageListener;
import org.springframework.kafka.support.Acknowledgment;

import analysis.AnalysisResult;
import execution.ExecutionJobResult;
import kafka.KafkaProducer;
import lombok.extern.slf4j.Slf4j;
import service.AnalyzerService;
import service.AnalyzerServiceFactory;

@Slf4j
public class ExecutionJobResultsListener implements AcknowledgingConsumerAwareMessageListener<String, ExecutionJobResult> {

	private KafkaProducer kafkaProducer;
	
	public ExecutionJobResultsListener(KafkaProducer kafkaProducer) {
		this.kafkaProducer = kafkaProducer;
	}
	
	@Override
	public void onMessage(ConsumerRecord<String, ExecutionJobResult> data, Acknowledgment acknowledgment, Consumer<?, ?> consumer) {
		
		log.info("Принято задание на анализ: " +
				"ID: "+data.value().getJobID() +
				", checkUnit: "+data.value().getCheckUnit().getValue() +
				", partition: "+data.partition() +
				", offset: "+data.offset());
		
		CompletableFuture.runAsync(() -> {
			ExecutionJobResult job = data.value();
			try {
        		AnalyzerService<? super ExecutionJobResult> service = AnalyzerServiceFactory.getService(job.getClass());
        		AnalysisResult analysisResult = service.analyzeResult(job);
        		kafkaProducer.sendAnalysisResult(analysisResult);
        		log.info("Анализ результата проверки ПС/ПАСД выполнен успешно : " + job.getJobID() + ", " + job.getCheckUnit().getValue());
        	} catch (Exception ex) {
        		log.error("Ошибка при обработке задания на анализ результатов проверки ПС/ПАСД : " + job.getJobID() + ", " + job.getCheckUnit().getValue(), ex);
        		kafkaProducer.sendErrorNotification(job.getJobID());
        	}
			acknowledgment.acknowledge();
		});
	}
}
