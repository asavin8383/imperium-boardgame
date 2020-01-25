package events;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

/**
 * Creation date: 06.08.2019
 * Author: asavin
 */
public interface DispatcherChannels {

    String INPUT_RESULTS = "inputResults";
    String ARRANGEMENT_EVENT = "arrangementEvents";

    @Input(INPUT_RESULTS)
    KStream<?, ?> results();

    @Output(ARRANGEMENT_EVENT)
    MessageChannel arrangementEvent();
}
