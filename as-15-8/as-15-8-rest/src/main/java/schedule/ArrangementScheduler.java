package schedule;

import model.enums.ExecutionStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import repositories.ArrangementRepository;

import java.time.LocalDateTime;

/**
 * Creation date: 24.05.2019
 * Author: asavin
 */

@Service
public class ArrangementScheduler {

    private ArrangementRepository arrangementRepository;

    public ArrangementScheduler(ArrangementRepository arrangementRepository) {
        this.arrangementRepository = arrangementRepository;
    }

    @Scheduled(cron = "${cron.expression.arrangement}")
    public void checkAndStartArrangementJobs(){
        arrangementRepository.findAllByExecutionStatusAndStartDateIsGreaterThan(ExecutionStatus.PLANNED, LocalDateTime.now())
            .forEach(arrangement ->  System.out.println(arrangement.getId()));
    }
}
