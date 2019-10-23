package controllers.helpers;

import jobs.ArrangementJob;
import enums.ExecutionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import model.task.Arrangement;
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
    public ArrangementExecutionHelper(ArrangementJobCreationService arrangementJobCreationService,
                                      ArrangementJobExecutionService arrangementJobExecutionService) {
        this.arrangementJobCreationService = arrangementJobCreationService;
        this.arrangementJobExecutionService = arrangementJobExecutionService;

    }

    public void sendJobToDispatcher(Arrangement arrangement){
        //Если запуск по плану - стартуем все ЕРДИ. Если повторный, ЕРДИ не заполняем, достаточно ИД мероприятия
        if (arrangement.getStatus().equals(ExecutionStatus.FORMED)){
            ArrangementJob arrangementJob = arrangementJobCreationService.createArrangementJob(arrangement);
            arrangementJobExecutionService.run(arrangementJob);
        }else if(arrangement.getStatus().equals(ExecutionStatus.ACTION_REQUIRED)){
            arrangementJobExecutionService.run(arrangementJobCreationService.createBriefArrangementJob(arrangement));
        }

    }

}
