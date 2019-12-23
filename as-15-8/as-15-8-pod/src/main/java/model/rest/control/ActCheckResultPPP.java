package model.rest.control;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ActCheckResultPPP {
    @JsonProperty("CheckResultId")
    private Long checkResultId;

    @JsonProperty("CheckUnitType")
    private String checkUnitType;

    @JsonProperty("CheckUnitValue")
    private String checkUnitValue;

    @JsonProperty("Date")
    private String date;    // 2019-08-15T18:58:00

    @JsonProperty("ContentId")
    private String contentId; // erdi_id из Таблицы sor.content

    @JsonProperty("IncludeTime")
    private String includeTime; // 2019-08-15T18:58:00

    @JsonProperty("ForbiddenContentDetected")
    private boolean forbiddenContentDetected;
}
