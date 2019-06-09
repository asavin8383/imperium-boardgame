package analysis;

import enums.CheckUnitJobResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
@NoArgsConstructor
public class AnonymizerAnalysisResult extends AnalysisResult implements StubAnalysisResult {

    private CheckUnitJobResult checkResult;

    private String errorCode;
    private Integer pageSize;

    private String etalonErrorCode;
    private Integer etalonPageSize;

    private String finalUrl;
    private String stubUrl;

    private Integer keyWordsCount;
    private Integer linkCount;
    private String stubScoreInfo;

    private Integer domainNameCount;
    private Integer similarityPercent;

    public boolean hasError() {
        return errorCode != null;
    }

    public boolean hasEtalonError() {
        return etalonErrorCode != null;
    }
}

