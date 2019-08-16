package arrangement;

import enums.ArrangementEvents;
import enums.ExecutionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Creation date: 28.05.2019
 * Author: asavin
 * Сообщение для возврата Rest-сервису информации об изменении состояния мероприятия
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ArrangementStatusNotification {

    /**Идентификатор мероприятия*/
    private Long arrangementId;

    /**Событие смены состояния мероприятия*/
    private ArrangementEvents event;
}
