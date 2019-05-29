package controllers.helpers;

import model.enums.ExecutionStatus;
import model.task.Arrangement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import services.arrangement.ArrangementJobCreationService;
import services.arrangement.ArrangementJobExecutionService;

/**
 * Creation date: 24.05.2019
 * Author: asavin
 * Инструмент запуска мероприятий
 */

@Service
public class ArrangementExecutionHelper {

    private ArrangementJobCreationService arrangementJobCreationService;
    private ArrangementJobExecutionService arrangementJobExecutionService;

    @Autowired
    public ArrangementExecutionHelper(ArrangementJobCreationService arrangementJobCreationService, ArrangementJobExecutionService arrangementJobExecutionService) {
        this.arrangementJobCreationService = arrangementJobCreationService;
        this.arrangementJobExecutionService = arrangementJobExecutionService;
    }

    public void sendJobToDispatcher(Arrangement arrangement){
        arrangementJobCreationService.createArrangementJobs(arrangement)
                .forEach(arrangementJobExecutionService::run);
    }

    /**
     * Устанавливает статус мероприятия в зависимости от текущего состояния его параметров
      * @param arrangement мероприятие
     */
    public void checkArrangementStatus(Arrangement arrangement){
        //Если новому мероприятию запланировали дату запуска в будущем,
        // при этом не пустой список ЕРДИ,
        // оно становится PLANNED
        if(arrangement.getStatus().equals(ExecutionStatus.NEW) &&
                arrangement.getStartDate() != null &&
                arrangement.getArrangementItems()!=null &&
                arrangement.getArrangementItems().size() > 0){
            arrangement.setStatus(ExecutionStatus.PLANNED);
            return;
        }
    }
}
