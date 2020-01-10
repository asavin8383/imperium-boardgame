package events.handlers;

import analysis.CheckUnitResult;
import checkUnits.CheckUnitKey;
import events.DispatcherChannels;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@Slf4j
@EnableBinding(DispatcherChannels.class)
public class ResultsHandler {

    @Value("${spring.cloud.stream.bindings.results_table.destination}")
    private String resultsTableName;

    @StreamListener(DispatcherChannels.INPUT_RESULTS)
    public void processResults(KStream<CheckUnitKey, CheckUnitResult> resultsStream){
        resultsStream
           .peek((key, result) ->
                log.info("\n   ---->>> Принято сообщение с анализом результатов проверки: " +
                    "мероприятие: " + key.getArrangementId() + ", " +
                    key.getJobId() + ", " + result.getCheckUnit().getValue() + ", результат: " + result.getCheckResult()))
            .mapValues(result -> {
                result.setEndTime(new Date());
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
}
