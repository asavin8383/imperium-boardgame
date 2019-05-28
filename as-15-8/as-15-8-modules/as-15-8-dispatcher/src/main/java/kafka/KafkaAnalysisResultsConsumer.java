package kafka;

import analysis.AnalysisResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

/**
 * Creation date: 27.05.2019
 * Author: asavin
 * Обработчик результатов анализа
 */

@Service
@Slf4j
public class KafkaAnalysisResultsConsumer {

    private KafkaTemplate<String, AnalysisResult> analysisResultKafkaTemplate;

    @Autowired
    public KafkaAnalysisResultsConsumer(KafkaTemplate<String, AnalysisResult> analysisResultKafkaTemplate) {
        this.analysisResultKafkaTemplate = analysisResultKafkaTemplate;
    }



}
