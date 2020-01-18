package arrangement;

import enums.ArrangementEvents;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDateTime;

/**
 * Creation date: 28.05.2019
 * Author: asavin
 * Сообщение для возврата Rest-сервису информации об изменении состояния мероприятия
 */

@Data
@NoArgsConstructor
public class ArrangementStatusNotification {

    /**Идентификатор мероприятия*/
    @NonNull
    private Long arrangementId;

    /**Событие смены состояния мероприятия*/
    @NonNull
    private ArrangementEvents event;

    /**Дополнительная информация о событии*/
    private String info = "";

    /**Дата события*/
    private LocalDateTime eventDate = LocalDateTime.now();

}
