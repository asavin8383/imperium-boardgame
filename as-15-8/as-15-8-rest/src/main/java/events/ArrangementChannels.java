package events;

import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.SubscribableChannel;

/**
 * Creation date: 06.08.2019
 * Author: asavin
 */
public interface ArrangementChannels {
    String OUTPUT_ARRANGEMENT_JOBS = "outputArrangementJobs";
    String INPUT_ARRANGEMENT_NOTIFICATIONS = "inputArrangementNotifications";

    @Output(OUTPUT_ARRANGEMENT_JOBS)
    MessageChannel outputArrangementJobs();

    @Input(INPUT_ARRANGEMENT_NOTIFICATIONS)
    SubscribableChannel inputArrangementNotifications();
}
