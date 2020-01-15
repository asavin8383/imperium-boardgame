package events.handlers;

import analysis.CheckUnitResult;
import checkUnits.CheckUnitKey;
import enums.CheckUnitJobResult;
import enums.ErdiStatus;
import events.DispatcherChannels;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.util.Date;

@Service
@Slf4j
@EnableBinding(DispatcherChannels.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ResultsHandler {

    @Value("${spring.cloud.stream.bindings.results_table.destination}")
    private String resultsTableName;

    private final ErdiChecker erdiChecker;

    @StreamListener(DispatcherChannels.INPUT_RESULTS)
    public void processResults(KStream<CheckUnitKey, CheckUnitResult> resultsStream){
        resultsStream
           .peek((key, result) ->
                log.info("\n   ---->>> Принято сообщение с анализом результатов проверки: " +
                    "мероприятие: " + key.getArrangementId() + ", " +
                    key.getJobId() + ", " + result.getCheckUnit().getValue() + ", результат: " + result.getCheckResult()))
            .mapValues(result -> {
                result.setEndTime(new Date());
                result.setCheckResult(checkErdiStatus(result.getCheckUnit().getContentId(), result.getCheckResult()));
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
}
