package events;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.messaging.SubscribableChannel;

/**
 * Creation date: 06.08.2019
 * Author: asavin
 */
public interface DispatcherChannels {
    String INPUT = "incomingArrangements";

    @Input(INPUT)
    SubscribableChannel incomingArrangements();
}
