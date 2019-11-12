package services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SchedulerService {

    private final MissionService missionService;

    @Scheduled(cron = "${spring.app.schedule.missions}")
    public void runMissionLoad() {
        log.info("[Scheduler] Запуск получения списка поручений из ППП Анонимайзера");
        missionService.fillMissions();
    }

}
