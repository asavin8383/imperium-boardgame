package services;

import jobs.ArrangementJob;

/**
 * Creation date: 23.05.2019
 * Author: asavin
 */
public interface ArrangementJobExecutionService {

    void run(ArrangementJob arrangementJob);
}
