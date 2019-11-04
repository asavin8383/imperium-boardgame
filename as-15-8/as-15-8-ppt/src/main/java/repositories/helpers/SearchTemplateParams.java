package repositories.helpers;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchTemplateParams {

    protected boolean containsInTraffic;
    protected Long trafficId;
    protected String template;

}
