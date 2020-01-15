package events;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.Input;

/**
 * Creation date: 06.08.2019
 * Author: asavin
 */
public interface DispatcherChannels {

    String INPUT_RESULTS = "inputResults";

    @Input(INPUT_RESULTS)
    KStream<?, ?> results();

}
