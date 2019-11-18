package arrangement;

import enums.ArrangementEvents;
import enums.ExecutionStatus;
import lombok.*;

import java.time.LocalDate;

/**
 * Creation date: 28.05.2019
 * Author: asavin
 * Сообщение для возврата Rest-сервису информации об изменении состояния мероприятия
 */

@Data
@NoArgsConstructor
@RequiredArgsConstructor
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
    private LocalDate eventDate = LocalDate.now();

    public ArrangementStatusNotification(Long arrangementId, ArrangementEvents event, String info){
        this.arrangementId = arrangementId;
        this.event = event;
        this.info = info;
    }
}
