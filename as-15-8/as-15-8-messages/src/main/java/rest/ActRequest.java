package rest;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class ActRequest {

    private Long arragementId;
    private String startDate;   // YYYY-MM-DDTHH24:MI:SS
    private String endDate;     // YYYY-MM-DDTHH24:MI:SS
    private String operatorName;
    private boolean isGeneratedAutomatically;

}
