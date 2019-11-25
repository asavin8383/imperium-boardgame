package model.rest.control;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class ActRequestPPP {
    @JsonProperty("MissionId")
    private Long missionId;

    @JsonProperty("ArragementId")
    private Long arrangementId;

    @JsonProperty("PSId")
    private Long psId;

    @JsonProperty("PASDId")
    private Long pasdId;

    @JsonProperty("PSName")
    private String psName;

    @JsonProperty("PASDName")
    private String pasdName;

    @JsonProperty("StartDate")
    private String startDate;   // YYYY-MM-DDTHH24:MI:SS

    @JsonProperty("EndDate")
    private String endDate;     // YYYY-MM-DDTHH24:MI:SS

    @ToString.Exclude
    @JsonProperty("CheckResultId")
    private List<ActCheckResultPPP> checkResults;
}
