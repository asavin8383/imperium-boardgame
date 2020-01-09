package events.handlers;

import analysis.CheckUnitResult;
import checkUnits.CheckUnitKey;
import events.DispatcherChannels;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.kafka.support.serializer.JsonSerde;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@EnableBinding(DispatcherChannels.class)
public class ResultsHandler {

    @Value("${spring.cloud.stream.bindings.results_table.destination}")
    private String resultsTableName;

    @StreamListener(DispatcherChannels.INPUT_RESULTS)
    public void processResults(KStream<CheckUnitKey, CheckUnitResult> resultsStream){
        resultsStream.peek((key, result) ->
                log.info("\n   ---->>> Принято сообщение с анализом результатов проверки: " +
                    "мероприятие: " + key.getArrangementId() + ", " +
                    result.getJobID() + ", " + result.getCheckUnit().getValue() + ", результат: " + result.getCheckResult()))
        .groupByKey(Grouped.with(new JsonSerde<>(), new JsonSerde<>()))
        .reduce((oldMessage, newMessage) -> newMessage,
                Materialized.<CheckUnitKey, CheckUnitResult, KeyValueStore<Bytes, byte[]>>
                    as(resultsTableName)
                    .withKeySerde(new JsonSerde<>())
                    .withValueSerde(new JsonSerde<>())
        );
    }
}
