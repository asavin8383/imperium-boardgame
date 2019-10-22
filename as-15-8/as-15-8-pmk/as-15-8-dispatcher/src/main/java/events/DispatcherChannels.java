package events;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * Creation date: 06.08.2019
 * Author: asavin
 */
public interface DispatcherChannels {
    String INPUT = "incomingArrangements";
    String NOTIFICATIONS_OUTPUT = "arrangementNotifications";

    @Input(INPUT)
    SubscribableChannel incomingArrangements();

    @Output(NOTIFICATIONS_OUTPUT)
    MessageChannel outputArrangementNotifications();
}
