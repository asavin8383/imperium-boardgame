package services.checkUnitJob;

import checkUnits.CheckUnitJob;
import jobs.ArrangementJob;

import java.util.List;

/**
 * Creation date: 24.05.2019
 * Author: asavin
 */
public interface CheckUnitJobService {

    List<CheckUnitJob> prepareJobs(ArrangementJob arrangementJob);
}
