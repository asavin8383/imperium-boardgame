package services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import restapi.ErdiRestClient;


@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class SchedulerService {

    private final MissionService missionService;
    private final ErdiRestClient erdiRestClient;

    @Scheduled(cron = "${spring.app.schedule.missions}")
    public void runMissionLoad() {
        boolean isLoading = missionService.getIsLoading();
        log.info("[Scheduler] Запуск обновления списка поручений из ППП Анонимайзера" +
                (isLoading ? "[пропущено, обновление еще не завершилось]" : ""));
        if (!isLoading)
            missionService.fillMissions();
    }

    @Scheduled(cron = "${spring.app.schedule.erdi}")
    public void runErdiUpdate() {
        boolean isLoading = erdiRestClient.getIsLoading();
        log.info("[Scheduler] Запуск обновления справочников ЕРДИ " + (isLoading ? "[пропущено, обновление еще не завершилось]" : ""));
        if (!isLoading)
            erdiRestClient.startUpdateErdi();
    }

}
