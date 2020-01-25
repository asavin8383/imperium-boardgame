package events;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

/**
 * Creation date: 06.08.2019
 * Author: asavin
 */
public interface DispatcherChannels {

    String INPUT_RESULTS = "inputResults";
    String OUTPUT_STOP_ARRANGEMENT_EVENT = "outputStopArrangementEvents";

    @Input(INPUT_RESULTS)
    KStream<?, ?> results();

    @Input(OUTPUT_STOP_ARRANGEMENT_EVENT)
    SubscribableChannel outputStopArrangementEvent();
}
