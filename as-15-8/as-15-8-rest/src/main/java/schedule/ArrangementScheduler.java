package schedule;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Creation date: 24.05.2019
 * Author: asavin
 */

@Service
@Slf4j
public class ArrangementScheduler {

   // private ArrangementRepository arrangementRepo;
   // private ArrangementExecutionHelper arrangementExecutionHelper;

   /* @Autowired
    public ArrangementScheduler(ArrangementRepository arrangementRepo, ArrangementExecutionHelper arrangementExecutionHelper) {
        this.arrangementRepo = arrangementRepo;
        this.arrangementExecutionHelper = arrangementExecutionHelper;
    }*/

    //@Scheduled(cron = "${cron.expression.arrangement}")
    //TODO Убрать запуск
    public void checkAndStartArrangementJobs(){
        /*arrangementRepo.findAllByStatusAndPlannedDateIsLessThan(ExecutionStatus.FORMED, LocalDateTime.now())
            .forEach(arrangement ->  {
                arrangementExecutionHelper.sendJobToDispatcher(arrangement);
                //arrangement.setStartDate(LocalDateTime.now());
                arrangement.sendEvent(ArrangementEvents.RUN);
                arrangementRepo.save(arrangement);
                log.info(String.format("Arrangement with id: %d was started", arrangement.getId()));
            });*/
    }
}
