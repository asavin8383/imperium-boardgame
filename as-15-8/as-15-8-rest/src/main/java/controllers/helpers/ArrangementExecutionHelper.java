package controllers.helpers;

import model.task.Arrangement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import services.ArrangementJobCreationService;
import services.ArrangementJobExecutionService;

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
}
