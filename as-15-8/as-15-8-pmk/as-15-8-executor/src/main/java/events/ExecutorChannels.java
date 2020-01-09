package events;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

public interface ExecutorChannels {

    String INPUT_JOBS = "jobs";
    String OUTPUT_EXECUTION_RESULTS = "executionResults";
    String OUTPUT_RESULTS = "results";

    @Input(INPUT_JOBS)
    SubscribableChannel jobs();

    @Output(OUTPUT_EXECUTION_RESULTS)
    MessageChannel executionResults();

    @Output(OUTPUT_RESULTS)
    MessageChannel results();
}
