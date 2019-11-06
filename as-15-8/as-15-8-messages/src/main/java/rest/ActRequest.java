package rest;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class ActRequest {
    public Long arragementId;

    @ToString.Exclude
    public List<ActCheckResult> checkResults;
    public String startDate;   // YYYY-MM-DDTHH24:MI:SS
    public String endDate;     // YYYY-MM-DDTHH24:MI:SS

    @ToString.Exclude
    public List<String> topScreenShots;
}
