package repositories.helpers;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomErdiParams {

    protected boolean belongsTo;
    protected Long trafficUnitId;
    protected String value;
    protected Long violationId;

}
