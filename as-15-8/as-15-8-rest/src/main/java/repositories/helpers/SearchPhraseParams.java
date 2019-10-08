package repositories.helpers;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchPhraseParams {

    protected boolean belongsTo;
    protected Long trafficUnitId;
    protected String phrase;
    protected Long violationId;

}
