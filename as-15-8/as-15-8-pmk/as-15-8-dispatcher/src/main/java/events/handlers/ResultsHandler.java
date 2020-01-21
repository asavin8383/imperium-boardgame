package events.handlers;

import analysis.AnalysisResult;
import analysis.CheckUnitResult;
import checkUnits.CheckUnitKey;
import enums.CheckUnitJobResult;
import enums.ErdiStatus;
import events.DispatcherChannels;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Screenshots;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Service;
import restapi.ErdiChecker;
import services.ResultService;

import java.util.Date;

@Service
@Slf4j
@EnableBinding(DispatcherChannels.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ResultsHandler {

    @Value("${spring.cloud.stream.bindings.results_table.destination}")
    private String resultsTableName;

    @Value("${spring.cloud.stream.bindings.screenshots_table.destination}")
    private String screenshotsTableName;

    @Value("${spring.cloud.stream.bindings.results_count_table.destination}")
    private String resultsCountTableName;

    private final ErdiChecker erdiChecker;
    private final ResultService resultService;

    @StreamListener(DispatcherChannels.INPUT_RESULTS)
    public void processResults(KStream<CheckUnitKey, CheckUnitResult> resultsStream){
        resultsStream
            .filter((checkUnitKey, checkUnitResult) -> checkUnitResult instanceof AnalysisResult)
            .mapValues(checkUnitResult ->
                new Screenshots(
                    ((AnalysisResult) checkUnitResult).getScreenshot(),
                    ((AnalysisResult) checkUnitResult).getEtalonScreenshot()
                )
            )
            .groupByKey()
            .reduce(
                (oldScreen, newScreen) -> newScreen,
                Materialized.<CheckUnitKey, Screenshots, KeyValueStore<Bytes, byte[]>>
                        as(screenshotsTableName)
                        .withKeySerde(new JsonSerde<>(CheckUnitKey.class))
                        .withValueSerde(new JsonSerde<>(Screenshots.class))
            );

        resultsStream
            .groupBy((checkUnitKey, checkUnitResult) -> checkUnitKey.getArrangementId())
            .count(
                Materialized.<Long, Long, KeyValueStore<Bytes, byte[]>>
                        as(resultsCountTableName)
                        .withKeySerde(Serdes.Long())
                        .withValueSerde(Serdes.Long())
            );

        resultsStream
            .peek((key, result) ->
                    log.info("\n   ---->>> Принято сообщение с анализом результатов проверки: " +
                            "мероприятие: " + key.getArrangementId() + ", " +
                            key.getJobId() + ", " + result.getCheckUnit().getValue() + ", результат: " + result.getCheckResult()))
            .mapValues((key, result) -> {
                result.setEndTime(new Date());
                result.setCheckResult(checkErdiStatus(result.getCheckUnit().getContentId(), result.getCheckResult()));
                if(result instanceof AnalysisResult){
                    ((AnalysisResult)result).setScreenshot(null);
                    ((AnalysisResult)result).setEtalonScreenshot(null);
                }
                return result;
            })
            .groupByKey()
            .reduce((oldMessage, newMessage) -> newMessage,
                    Materialized.<CheckUnitKey, CheckUnitResult, KeyValueStore<Bytes, byte[]>>
                            as(resultsTableName)
                            .withKeySerde(new JsonSerde<>(CheckUnitKey.class))
                            .withValueSerde(new JsonSerde<>(CheckUnitResult.class))
            );
    }

    private CheckUnitJobResult checkErdiStatus(Long contentId, CheckUnitJobResult status){
        if(erdiChecker.checkErdiStatus(contentId).equals(ErdiStatus.EXCLUDED))
            return CheckUnitJobResult.EXCLUDED;
        else
            return status;
    }

    private void saveArrangementIfFinished(Long arrangementId) {

    }
}
