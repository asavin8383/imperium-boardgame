package repositories.helpers;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FormalErdiParams {

    protected boolean belongsTo;
    protected Long trafficUnitId;
    protected String value;
    protected Long resourceTypeId;

}
