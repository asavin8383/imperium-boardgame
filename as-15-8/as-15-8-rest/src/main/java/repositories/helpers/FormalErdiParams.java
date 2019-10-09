package repositories.helpers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormalErdiParams {

    protected boolean returnAll;
    protected Long erdiTrafficUnitId;
    protected Long searchTrafficUnitId;
    protected String value;
    protected Long resourceTypeId;

}
