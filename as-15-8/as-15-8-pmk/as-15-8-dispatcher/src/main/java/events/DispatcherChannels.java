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
    String ARRANGEMENTS_INPUT = "incomingArrangements";
    String ANALYSIS_RESULTS_INPUT = "inputAnalysisResults";
    String NOTIFICATIONS_OUTPUT = "arrangementNotifications";

    @Input(ARRANGEMENTS_INPUT)
    SubscribableChannel incomingArrangements();

    @Input(ANALYSIS_RESULTS_INPUT)
    SubscribableChannel inputAnalysisResults();

    @Output(NOTIFICATIONS_OUTPUT)
    MessageChannel outputArrangementNotifications();
}
