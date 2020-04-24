package services.arrangement;

import arrangement.ArrangementStatusNotification;
import enums.ArrangementEvents;

/**
 * Creation date: 29.05.2019
 * Author: asavin
 * Обработчик уведомлений о смене состояния мероприятий
 */

public interface ArrangementNotificationService {

    boolean processNotification(ArrangementStatusNotification arrangementStatusNotification);
    boolean processNotificationInPPT(ArrangementStatusNotification arrangementStatusNotification);
    ArrangementStatusNotification createNotification(Long arrangementId, ArrangementEvents event, Long completion);
}
