package services;

import java.util.List;

import checkUnits.CheckUnitJob;
import jobs.ArrangementJob;

/**
 * Сервис работы с заданиями на проверку запрещенных ресурсов
 * Author: asavin
 */
public interface CheckUnitJobService {

    List<CheckUnitJob> prepareJobs(ArrangementJob arrangementJob);

}
