package events.producers.kafka;

import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;

/**
 * Created by san
 * Date: 05.11.2019
 */
public interface PPM_Channels {
    String OUTPUT_JOBS = "outputJobs";

    @Output(OUTPUT_JOBS)
    MessageChannel outputJobs();
}
