package services.arrangement;

import jobs.ArrangementJob;
import model.task.Arrangement;

import java.util.List;

/**
 * Creation date: 23.05.2019
 * Author: asavin
 */
public interface ArrangementJobCreationService {

    List<ArrangementJob> createArrangementJobs(Arrangement arrangement);
}
