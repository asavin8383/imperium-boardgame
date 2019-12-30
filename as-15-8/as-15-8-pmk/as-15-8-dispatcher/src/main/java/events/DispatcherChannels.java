package events;

import org.apache.kafka.streams.kstream.KStream;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * Creation date: 06.08.2019
 * Author: asavin
 */
public interface DispatcherChannels {

    String INPUT_ANALYSIS_RESULTS = "inputAnalysisResults";
    String INPUT_JOB_NOTIFICATIONS = "inputJobNotifications";

    @Input(INPUT_ANALYSIS_RESULTS)
    KStream<?, ?> inputAnalysisResults();

    @Input(INPUT_JOB_NOTIFICATIONS)
    KStream<?, ?> inputJobNotifications();

}
