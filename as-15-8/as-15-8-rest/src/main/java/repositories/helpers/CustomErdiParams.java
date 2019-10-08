package repositories.helpers;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomErdiParams {

    protected boolean returnAll;
    protected Long erdiTrafficUnitId;
    protected Long searchTrafficUnitId;
    protected String value;
    protected Long violationId;

}
