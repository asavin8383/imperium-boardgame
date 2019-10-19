package model.rest.control;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class PodState {
    String dateUpdate;                  // дата изменения (для отображения на фронте)
    String actualServerDumpDate;        // дата актуального дампа на сервере
    String state;                       // состояние null/PROCESS/ERROR
    String errorMessage;                // сообщение об ошибке, если состояние = ERROR
    String stateDetails;                // детальная информация о процессе
    Long lastContentVersion;
    Long lastAddonVersion;
    List<Long> contentVersions;
}
