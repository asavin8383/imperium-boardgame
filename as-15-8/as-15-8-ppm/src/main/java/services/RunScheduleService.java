package services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import repositories.SchedulePeriodRepo;
import repositories.ScheduleRepo;

/**
 * Created by san
 * Date: 04.11.2019
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RunScheduleService {

    private final ScheduleRepo scheduleRepo;
    private final SchedulePeriodRepo schedulePeriodRepo;


    @Scheduled(cron = "${app.schedule}")
    public void runSchedule(){

    }
}
