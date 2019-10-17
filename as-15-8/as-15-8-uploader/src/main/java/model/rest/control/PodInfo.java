package model.rest.control;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.Date;


@Data
@AllArgsConstructor
public class PodInfo {

    Date lastUpdateDate;
    Date lastDumpDate;
}
