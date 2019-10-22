package events;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface AnalyzerChannels {

    String INPUT = "input";
    String OUTPUT= "output";
    String OUTPUT_NOTIFICATIONS = "notifications";

    @Input(INPUT)
    SubscribableChannel input();

    @Output(OUTPUT)
    MessageChannel output();

    @Output(OUTPUT_NOTIFICATIONS)
    MessageChannel notifications();
}
