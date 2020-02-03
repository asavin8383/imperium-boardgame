package services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.SystemMode;
import model.enums.SystemModeUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import repositories.SystemModesRepository;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class SystemModeService {

    private final SystemModesRepository systemModesRepository;
    private final TaskScheduler scheduler;

    /*@PostConstruct
    public void planAtServiceStarting() {
        Optional<SystemMode> mode = systemModesRepository.findBySystemMode(SystemModeUnit.SERVICE);
        if (mode.isPresent() && mode.get().getPlannedDateTime() != null) {
            planServiceModeChange(mode.get());
        }
    }*/

    @Async
    public void planServiceModeChange(SystemMode mode) {
        mode.setActive(false);
        systemModesRepository.save(mode);
        scheduler.schedule(changeMode(mode), asDate(mode.getPlannedDateTime()));
    }

    private Runnable changeMode(SystemMode mode) {
        return () -> {
            mode.setActive(true);
            systemModesRepository.save(mode);
        };
    }

    private Date asDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

}
