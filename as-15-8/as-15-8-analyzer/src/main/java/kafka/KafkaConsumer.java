package kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import execution.ExecutionPSJobResult;
import lombok.extern.slf4j.Slf4j;
import model.ArrangementResult;
import service.AnalyzerService;
import service.AnalyzerServiceFactory;

@Service
@Slf4j
public class KafkaConsumer {
	
	@KafkaListener(topics = "${spring.kafka.consume-topic}")
    public void consumeExecutionPSJobMessage(ExecutionPSJobResult job/*, Acknowledgment ack*/) {
		log.info("Принято задание на анализ результата проверки ПС/ПАСД: " + job.getCheckUnit().getValue());
        CompletableFuture.runAsync(() -> {
        	try {
        		AnalyzerService service = AnalyzerServiceFactory.getService(job.getClass());
        		ArrangementResult check = service.analyzeResult(job);
        		service.writeCheckResult(check);
        		log.info("Анализ результата проверки ПС/ПАСД выполнен успешно : " + job.getCheckUnit().getValue());
        		//ack.acknowledge();
        	} catch (Exception ex) {
        		log.error("Ошибка при обработке задания на анализ результатов проверки ПС/ПАСД : " + job.getCheckUnit().getValue(), ex);
        	}
        });
    }
	
}
