package services.arrangement;

import arrangement.ArrangementStatusNotification;

/**
 * Creation date: 29.05.2019
 * Author: asavin
 * Обработчик уведомлений о смене состояния мероприятий
 */

public interface ArrangementNotificationService {

    boolean processNotification(ArrangementStatusNotification arrangementStatusNotification);
}
