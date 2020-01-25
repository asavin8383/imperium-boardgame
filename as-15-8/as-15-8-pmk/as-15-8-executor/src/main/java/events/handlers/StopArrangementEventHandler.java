package events.handlers;

import events.ExecutorChannels;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Service;
import remoteEvents.ArrangementStopEvent;
import service.JobsService;

@Service
@EnableBinding({ExecutorChannels.class})
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class StopArrangementEventHandler {

    private final JobsService jobsService;

    @StreamListener(ExecutorChannels.ARRANGEMENT_EVENTS)
    public void consumeCheckUnitJob(ArrangementStopEvent event) {
        jobsService.stop(event.getArrangementId(), event.getVersion());
    }
}
