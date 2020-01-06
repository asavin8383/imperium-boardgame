package events.handlers;

import analysis.CheckUnitResult;
import checkUnits.CheckUnitKey;
import events.DispatcherChannels;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@EnableBinding(DispatcherChannels.class)
public class ResultsHandler {

    @Value("${spring.cloud.stream.bindings.results_table.destination}")
    private String resultsTableName;

    @StreamListener
    public void processResults(
            @Input(DispatcherChannels.INPUT_ANALYSIS_RESULTS) KStream<CheckUnitKey, CheckUnitResult> analysisResultsStream,
            @Input(DispatcherChannels.INPUT_JOB_NOTIFICATIONS) KStream<CheckUnitKey, CheckUnitResult> notificationsStream
    ){
        analysisResultsStream.peek((key, result) -> {
            log.info("\n   ---->>> Принято сообщение с анализом результатов проверки: " +
                    "мероприятие: " + key.getArrangementId() + ", " +
                    result.getJobID() + ", " + result.getCheckUnit().getValue() + ", результат: " + result.getCheckResult());
        }).merge(notificationsStream
            .peek((key, result) -> {
                    log.info("\n   ---->>> Принято сообщение с уведомлением от проверки: " +
                            "мероприятие: " + key.getArrangementId() + ", " +
                            result.getJobID() + ", " + result.getCheckUnit().getValue() + ", результат: " + result.getCheckResult());
            })
        ).groupByKey()
        .reduce((oldMessage, newMessage) -> newMessage,
                Materialized.<CheckUnitKey, CheckUnitResult, KeyValueStore<Bytes, byte[]>>
                    as(resultsTableName)
                    .withKeySerde(Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>()))
                    .withValueSerde(Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>()))
        );
    }
}
