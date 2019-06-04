package controllers.helpers;

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
        arrangementJobCreationService.createArrangementJobs(arrangement)
                .forEach(arrangementJobExecutionService::run);
    }

}
