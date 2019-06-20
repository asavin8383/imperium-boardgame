package kafka;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import analysis.AnalysisResult;
import arrangement.ArrangementStatusNotification;
import enums.ArrangementStatus;
import enums.CheckUnitJobResult;
import lombok.extern.slf4j.Slf4j;
import model.ArrangementResult;
import services.CheckUnitJobService;

/**
 * Creation date: 27.05.2019
 * Author: asavin
 * Обработчик результатов анализа
 */

@Service
@Slf4j
public class KafkaAnalysisResultsConsumer {

	@Autowired
	private CheckUnitJobService checkUnitService;
	
	@Autowired
	private ArrangementStatusProducer arrangementStatusProducer;
	
    @KafkaListener(
    	topics = "${spring.kafka.analysis-results-topic}",
    	containerFactory = "kafkaAnalysisResultListenerContainerFactory"
    )
    public void consumeAnalysisResults(AnalysisResult analysisResult, Acknowledgment ack) {
		log.info("Принято сообщение с анализом результатов проверки: " + analysisResult.getJobID() + ", " + analysisResult.getCheckUnit().getValue());
    	try {       		
    		ArrangementResult jobResult = checkUnitService.processJobResult(analysisResult);
    		log.info("Результаты выполнения проверки успешно обработаны: " + analysisResult.getJobID() + ", " + analysisResult.getCheckUnit().getValue());
    		
    		ArrangementStatus arrStatus = checkUnitService.checkArrangementStatus(jobResult.getArrangementId());
    		if(arrStatus == ArrangementStatus.FINISHED) {
    			log.info("Мероприятие успешно завешено: " + jobResult.getArrangementId());
    			arrangementStatusProducer.sendArrangementStatusMessage(new ArrangementStatusNotification(jobResult.getArrangementId(), arrStatus));
    		}

    	} catch (Exception ex) {
    		try {
        		log.error("Ошибка при обработке сообщения с анализом результатов проверки: " + analysisResult.getJobID() + ", " + analysisResult.getCheckUnit().getValue(), ex);
        		
        		StringWriter sw = new StringWriter();
        		ex.printStackTrace(new PrintWriter(sw));
        		
        		ArrangementResult jobResult = checkUnitService.updateJobStatus(analysisResult.getJobID(), CheckUnitJobResult.INTERNAL_ERROR, sw.toString());
        		ArrangementStatus arrStatus = checkUnitService.checkArrangementStatus(jobResult.getArrangementId());
        		if(arrStatus == ArrangementStatus.FINISHED) {
        			log.info("Мероприятие успешно завешено: " + jobResult.getArrangementId());
        			arrangementStatusProducer.sendArrangementStatusMessage(new ArrangementStatusNotification(jobResult.getArrangementId(), arrStatus));
        		}
    		} catch(Exception newEx) {
    			log.error("Ошибка при сохранении ошибочной обработки сообщения с анализом результатов проверки: " + analysisResult.getJobID() + ", " + analysisResult.getCheckUnit().getValue(), newEx);
    		}
    	}
    	ack.acknowledge();
    }
}
