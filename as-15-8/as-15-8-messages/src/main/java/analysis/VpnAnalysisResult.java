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
public class VpnAnalysisResult extends AnalysisResult implements StubAnalysisResult {

	private CheckUnitJobResult checkResult;

	private String responseErrorCode;
	private String responseErrorCodeEtalon;
	private Boolean responseError;
	private Boolean useEtalon;
	private Integer httpStatus;
	private Integer pageSize;
	private Integer pageSizeEtalon;
	private Integer keyWordsCount;
	private Integer linkCount;
	private Integer domainNameCount;
	private String pageUrlFinal;
	private String pageUrlFinalEtalon;
	private Boolean redirectionDetected;
	private String stubUrl;
	private Integer similarityOriginPercent;
	private Boolean needTestFinalUrl;
	private String stubScoreInfo;

	@Override
	public String getFinalUrl() {
		return pageUrlFinal;
	}

    public boolean hasError() {
        return responseErrorCode != null;
    }

    public boolean hasEtalonError() {
        return responseErrorCodeEtalon != null;
    }
}
