package model.rest.control;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class ActRequestPPP {
    private Long missionId;
    private Long arragementId;
    private Long psId;
    private Long pasdId;
    private String psName;
    private String pasdName;
    private String startDate;   // YYYY-MM-DDTHH24:MI:SS
    private String endDate;     // YYYY-MM-DDTHH24:MI:SS

    @ToString.Exclude
    private List<ActCheckResultPPP> checkResults;
}
