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

    /**
     * Сохранение задания на проверку в базу,
     * чтобы потом можно было приложить к нему результат и скриншот
     * @param checkUnitJob
     */
    Long saveCheckUnitJobAsResult(CheckUnitJob checkUnitJob);
}
