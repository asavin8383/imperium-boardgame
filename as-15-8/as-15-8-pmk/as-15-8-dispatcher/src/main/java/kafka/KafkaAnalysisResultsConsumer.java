package kafka;

import analysis.AnalysisResult;
import arrangement.ArrangementStatusNotification;
import enums.ArrangementEvents;
import enums.CheckUnitJobResult;
import enums.ExecutionStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.ArrangementResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import services.ArrangementResultService;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Creation date: 27.05.2019
 * Author: asavin
 * Обработчик результатов анализа
 */

//TODO удалить
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class KafkaAnalysisResultsConsumer {

	private final ArrangementResultService arrangementResultService;
	private final ArrangementStatusProducer arrangementStatusProducer;
	
    @KafkaListener(
    	topics = "${spring.kafka.analysis-results-topic}",
    	containerFactory = "kafkaAnalysisResultListenerContainerFactory"
    )
    public void consumeAnalysisResults(AnalysisResult analysisResult, Acknowledgment ack) {
		log.info("Принято сообщение с анализом результатов проверки: " + analysisResult.getJobID() + ", " + analysisResult.getCheckUnit().getValue());
    	try {       		
    		ArrangementResult jobResult = arrangementResultService.processJobResult(analysisResult);
    		log.info("Результаты выполнения проверки успешно обработаны: " + analysisResult.getJobID() + ", " + analysisResult.getCheckUnit().getValue());
    		
    		ExecutionStatus status = arrangementResultService.checkArrangementStatus(jobResult.getArrangementId());
    		if(status == ExecutionStatus.FINISHED) {
    			log.info("Мероприятие успешно завешено: " + jobResult.getArrangementId());
    			arrangementStatusProducer.sendArrangementStatusMessage(new ArrangementStatusNotification(jobResult.getArrangementId(), ArrangementEvents.FINISH));
    		}

    	} catch (Exception ex) {
    		try {
        		log.error("Ошибка при обработке сообщения с анализом результатов проверки: " + analysisResult.getJobID() + ", " + analysisResult.getCheckUnit().getValue(), ex);
        		
        		StringWriter sw = new StringWriter();
        		ex.printStackTrace(new PrintWriter(sw));
        		
        		ArrangementResult jobResult = arrangementResultService.updateJobStatus(analysisResult.getJobID(), CheckUnitJobResult.INTERNAL_ERROR, sw.toString());
				ExecutionStatus status = arrangementResultService.checkArrangementStatus(jobResult.getArrangementId());
        		if(status == ExecutionStatus.FINISHED) {
        			log.info("Мероприятие успешно завешено: " + jobResult.getArrangementId());
        			arrangementStatusProducer.sendArrangementStatusMessage(new ArrangementStatusNotification(jobResult.getArrangementId(), ArrangementEvents.FINISH));
        		}
    		} catch(Exception newEx) {
    			log.error("Ошибка при сохранении ошибочной обработки сообщения с анализом результатов проверки: " + analysisResult.getJobID() + ", " + analysisResult.getCheckUnit().getValue(), newEx);
    		}
    	}
    	ack.acknowledge();
    }
}
