package kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import enums.ArrangementUnitCheckResult;
import execution.ExecutionJobResult;
import execution.ExecutionPSJobResult;
import lombok.extern.slf4j.Slf4j;
import model.ArrangementResult;
import repositories.ArrangementResultRepository;
import service.AnalyzerService;
import service.AnalyzerServiceFactory;

@Service
@Slf4j
public class KafkaConsumer {
	
	@Autowired
	private ArrangementResultRepository repository;
	
	@KafkaListener(topics = "${spring.kafka.consume-topic}")
    public void consumeExecutionPSJobMessage(ExecutionPSJobResult job, Acknowledgment ack) {
		log.info("Принято задание на анализ результата проверки ПС/ПАСД: " + job.getCheckUnit().getValue());
        CompletableFuture.runAsync(() -> {
        	try {
        		AnalyzerService service = AnalyzerServiceFactory.getService(job.getClass());
        		ArrangementResult check = service.analyzeResult(job);
        		repository.save(check);
        		log.info("Анализ результата проверки ПС/ПАСД выполнен успешно : " + job.getCheckUnit().getValue());
        	} catch (Exception ex) {
        		log.error("Ошибка при обработке задания на анализ результатов проверки ПС/ПАСД : " + job.getCheckUnit().getValue(), ex);
        		writeException(job);
        	}
        	ack.acknowledge();
        });
    }
	
	private void writeException(ExecutionJobResult execResult) {
		ArrangementResult arrRes = new ArrangementResult();
		arrRes.setArrangementId(execResult.getArrangenmentID());
		arrRes.setErdiId(execResult.getErdiID());
		arrRes.setCheckUnitType(execResult.getCheckUnit().getType());
		arrRes.setCheckUnitValue(execResult.getCheckUnit().getValue());
		arrRes.setResult(ArrangementUnitCheckResult.INTERNAL_ERROR);
		repository.save(arrRes);
	}
}
