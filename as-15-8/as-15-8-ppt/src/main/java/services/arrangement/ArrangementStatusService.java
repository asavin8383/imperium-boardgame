package services.arrangement;

import model.task.Arrangement;

/**
 * Creation date: 04.06.2019
 * Author: asavin
 * Механизм изменения статусов мероприятий
 */
public interface ArrangementStatusService {

    void processArrangementStatusChange(Arrangement arrangement);
}
