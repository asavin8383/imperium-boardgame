package repositories.helpers;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchPhraseParams {

    protected boolean returnAll;
    protected Long searchTrafficUnitId;
    protected String phrase;
    protected Long violationId;

}
