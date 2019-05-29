package arrangement;

import enums.ArrangementStatus;
import lombok.Data;

/**
 * Creation date: 28.05.2019
 * Author: asavin
 * Сообщение для возврата Rest-сервису информации об изменении состояния мероприятия
 */

@Data
public class ArrangementStatusNotification {

    /**Идентификатор мероприятия*/
    private Long arrangementId;

    /**Текущее состояние мереприятия*/
    private ArrangementStatus arrangementStatus;
}
