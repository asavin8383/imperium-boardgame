package analysis;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;


@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
@NoArgsConstructor
public class PureChannelAnalysisResult extends AnalysisResult {

	private String resultNLP;
	private String responseErrorCode;
	private Boolean responseError;
	private Boolean useStubUrl;
	private Integer httpStatus;
	private String httpHeaders;
	private Integer pageSize;
	private Integer keyWordsCount;
	private Integer linkCount;
	private Integer domainNameCount;
	private String pageUrlFinal;
	private Boolean redirectionDetected;
	private Integer similarityOriginPercent;
	private Boolean needTestFinalUrl;
	private Boolean forbiddenFinalUrl;

	public boolean hasError() {
        return responseErrorCode != null;
    }
}
