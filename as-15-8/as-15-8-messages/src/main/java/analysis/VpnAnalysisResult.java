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
public class VpnAnalysisResult extends AnalysisResult {

	private CheckUnitJobResult checkResult;

	private String responseErrorCode;
	private Boolean responseError;
	private Integer httpStatus;
	private Integer pageSize;
	private Integer pageSizeEtalon;
	private Integer keyWordsCount;
	private Integer linkCount;
	private Integer domainNameCount;
	private String pageUrlFinal;
	private Integer similarityOriginPercent;
	private String stubScoreInfo;
}
