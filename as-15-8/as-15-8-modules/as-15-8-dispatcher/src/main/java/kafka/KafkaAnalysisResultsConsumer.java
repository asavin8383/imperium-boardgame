package kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import analysis.AnalysisResult;
import exceptions.AS_15_8_DispatcherException;
import lombok.extern.slf4j.Slf4j;
import model.ArrangementResult;
import repositories.ArrangementResultRepository;
import services.AnalysisResultService;
import services.AnalysisResultServiceFactory;

/**
 * Creation date: 27.05.2019
 * Author: asavin
 * Обработчик результатов анализа
 */

@Service
@Slf4j
public class KafkaAnalysisResultsConsumer {

	@Autowired
	private ArrangementResultRepository jobsRepo;
	
    @KafkaListener(
    	topics = "${spring.kafka.analysis-results-topic}",
    	containerFactory = "kafkaAnalysisResultListenerContainerFactory"
    )
    public void consumeAnalysisResults(AnalysisResult analysisResult, Acknowledgment ack) {
		log.info("Принято сообщение с анализом результатов проверки: " + analysisResult.toString());
        CompletableFuture.runAsync(() -> {
        	try {
				//TODO написать сохранение результатов и проверку, выполнено ли мероприятие полностью
        		
        		ArrangementResult job = jobsRepo.findById(analysisResult.getJobID())
        			.orElseThrow(() -> 
        				new AS_15_8_DispatcherException("Ошибка! Задание не найдено! ID: " + analysisResult.getJobID())
        			);
        		
        		AnalysisResultService<? super AnalysisResult> service = AnalysisResultServiceFactory.getService(analysisResult.getClass());
        		job.setResult(service.processResult(analysisResult));  		
        		job.setScreenshot(analysisResult.getScreenshot());
        		jobsRepo.save(job);
        		log.info("Результаты выполнения проверки успешно сохранены: " + analysisResult.getJobID());
        	} catch (Exception ex) {
        		log.error("Ошибка при обработке задания на проведение мероприятия: " + analysisResult.toString(), ex);
        	}
        	ack.acknowledge();
        });
    }
}
