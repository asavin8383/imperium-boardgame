package kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import analysis.AnalysisResult;
import lombok.extern.slf4j.Slf4j;

/**
 * Creation date: 27.05.2019
 * Author: asavin
 * Обработчик результатов анализа
 */

@Service
@Slf4j
public class KafkaAnalysisResultsConsumer {

    @KafkaListener(topics = "${spring.kafka.analysis-results-topic}")
    public void consumeAnalysisResults(AnalysisResult analysisResult, Acknowledgment ack) {
		log.info("Принято сообщение с анализом результатов проверки: " + analysisResult.toString());
        CompletableFuture.runAsync(() -> {
        	try {
				//TODO написать сохранение результатов и проверку, выполнено ли мероприятие полностью
        	} catch (Exception ex) {
        		log.error("Ошибка при обработке задания на проведение мероприятия: " + analysisResult.toString(), ex);
        	}
        	ack.acknowledge();
        });
    }

}
