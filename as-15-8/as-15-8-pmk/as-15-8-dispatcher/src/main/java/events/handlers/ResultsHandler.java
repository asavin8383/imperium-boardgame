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

    public static final String RESULT_TABLE_NAME = "results_table";

    @StreamListener
    public void processResults(
            @Input(DispatcherChannels.INPUT_ANALYSIS_RESULTS) KStream<CheckUnitKey, Message<CheckUnitResult>> analysisResultsStream,
            @Input(DispatcherChannels.INPUT_JOB_NOTIFICATIONS) KStream<CheckUnitKey, Message<CheckUnitResult>> notificationsStream
    ){
        analysisResultsStream.mapValues(message -> {
            CheckUnitResult result = message.getPayload();
            log.info("\n   ---->>> Принято сообщение с анализом результатов проверки: " +
                    result.getJobID() + ", " + result.getCheckUnit().getValue() + ", результат: " + result.getCheckResult() +
                    ". partition: " + message.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID, Integer.class) +
                    ", offset: " + message.getHeaders().get(KafkaHeaders.OFFSET, Long.class));
            return result;
        }).merge(notificationsStream
            .mapValues(message -> {
                    CheckUnitResult result = message.getPayload();
                    log.info("\n   ---->>> Принято сообщение с уведомлением от проверки: " +
                            result.getJobID() + ", " + result.getCheckUnit().getValue() + ", результат: " + result.getCheckResult() +
                            ", partition: "+message.getHeaders().get(KafkaHeaders.RECEIVED_PARTITION_ID, Integer.class) +
                            ", offset: "+message.getHeaders().get(KafkaHeaders.OFFSET, Long.class));
                    return result;
            })
        ).groupByKey()
        .reduce((oldMessage, newMessage) -> newMessage,
                Materialized.<CheckUnitKey, CheckUnitResult, KeyValueStore<Bytes, byte[]>>
                    as(RESULT_TABLE_NAME)
                    .withKeySerde(Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>()))
                    .withValueSerde(Serdes.serdeFrom(new JsonSerializer<>(), new JsonDeserializer<>()))
        );
    }
}
