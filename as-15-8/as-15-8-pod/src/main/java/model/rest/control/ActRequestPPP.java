package model.rest.control;

import lombok.Data;
import lombok.ToString;

import java.util.List;

@Data
@ToString
public class ActRequestPPP {
    public Long MissionId;
    public Long ArragementId;
    public Long PSId;
    public Long PASDId;
    public String PSName;
    public String PASDName;
    public String StartDate;   // YYYY-MM-DDTHH24:MI:SS
    public String EndDate;     // YYYY-MM-DDTHH24:MI:SS
    public List<ActCheckResultPPP> CheckResults;
}
