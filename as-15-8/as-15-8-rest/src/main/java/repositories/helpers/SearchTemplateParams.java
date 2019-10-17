package repositories.helpers;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchTemplateParams {

    protected boolean belongsTo;
    protected Long trafficUnitId;
    protected String template;

}
