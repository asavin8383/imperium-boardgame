package rest;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class ActRequest {

    private Long arragementId;

//    @ToString.Exclude
//    public List<ActCheckResult> checkResults;
    private String startDate;   // YYYY-MM-DDTHH24:MI:SS
    private String endDate;     // YYYY-MM-DDTHH24:MI:SS

//    @ToString.Exclude
//    public List<String> topScreenShots;
}
