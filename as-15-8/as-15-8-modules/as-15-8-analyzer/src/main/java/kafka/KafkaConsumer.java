package kafka;

import javax.annotation.PostConstruct;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import analysis.AnalysisResult;
import enums.AccessToolUnit;
import execution.ExecutionJobResult;
import lombok.extern.slf4j.Slf4j;
import service.AnalyzerService;
import service.AnalyzerServiceFactory;

@Service
@DependsOn({"analyzerServiceFactory"})
@Slf4j
public class KafkaConsumer {
	
	@Value("${spring.kafka.consume-topic}")
	private String executionResultsTopicName;
	
	@Autowired
	private KafkaProducer kafkaProducer;
	
	@Autowired
	private ConcurrentKafkaListenerContainerFactory<String, ExecutionJobResult> kafkaListenerContainerFactory;
	
	@Autowired
	private KafkaListenerEndpointRegistry endpointRegistry;
	
	@PostConstruct
    void createExecutionJobResultsListeners() {
    	
    	for(AccessToolUnit accessTool : AccessToolUnit.values()) {
    		    	
    		ExecutionJobResultListenerEndpoint endpoint = new ExecutionJobResultListenerEndpoint(
    				executionResultsTopicName,
    				accessTool,
    				(data, ack) -> {
    					consumeExecutionJobResultsMessage(data, ack);
    				}
        		);
        		
        	endpointRegistry.registerListenerContainer(endpoint, kafkaListenerContainerFactory);
    	}
    }
	
	private void consumeExecutionJobResultsMessage(ConsumerRecord<String, ExecutionJobResult> data, Acknowledgment ack) {
		log.info("Принято задание на анализ: " +
				"ID: "+data.value().getJobID() +
				", checkUnit: "+data.value().getCheckUnit().getValue() +
				", partition: "+data.partition() +
				", offset: "+data.offset());
		
		ExecutionJobResult job = data.value();
		try {
    		AnalyzerService<? super ExecutionJobResult> service = AnalyzerServiceFactory.getService(job.getClass());
    		AnalysisResult analysisResult = service.analyzeResult(job);
    		kafkaProducer.sendAnalysisResult(analysisResult);
    		log.info("Анализ результата проверки ПС/ПАСД выполнен успешно : " + job.getJobID() + ", " + job.getCheckUnit().getValue());
    	} catch (Exception ex) {
    		log.error("Ошибка при обработке задания на анализ результатов проверки ПС/ПАСД : " + job.getJobID() + ", " + job.getCheckUnit().getValue(), ex);
    		kafkaProducer.sendErrorNotification(job.getJobID(), ex);
    	}
		ack.acknowledge();
	}
}
