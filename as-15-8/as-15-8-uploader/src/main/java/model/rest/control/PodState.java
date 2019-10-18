package model.rest.control;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PodState {
    String dateUpdate;          // дата изменения
    String actualDumpDate;      // дата актуального дампа на сервере
    String state;               // состояние null/PROCESS/ERROR
    String errorMessage;        // сообщение об ошибке, если состояние = ERROR
    Long lastContentVersion;
    Long lastAddonVersion;
}
