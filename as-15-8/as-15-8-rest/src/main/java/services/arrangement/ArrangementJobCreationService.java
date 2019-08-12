package services.arrangement;

import jobs.ArrangementJob;
import model.task.Arrangement;

/**
 * Creation date: 23.05.2019
 * Author: asavin
 */
public interface ArrangementJobCreationService {

    ArrangementJob createBriefArrangementJob(Arrangement arrangement);

    ArrangementJob createArrangementJob(Arrangement arrangement);



}
