package events.handlers;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;
import remoteEvents.ArrangementStopEvent;
import service.JobsService;

@Component
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class StopArrangementEventHandler implements ApplicationListener<ArrangementStopEvent> {

    private final JobsService jobsService;

    @Override
    public void onApplicationEvent(ArrangementStopEvent event) {
        jobsService.stop(event.getArrangementId(), event.getVersion());
    }
}
