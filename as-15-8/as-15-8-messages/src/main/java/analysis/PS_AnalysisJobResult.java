package analysis;

import enums.ArrangementUnitCheckResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
@NoArgsConstructor
public class PS_AnalysisJobResult extends AnalysisResult {

	private ArrangementUnitCheckResult checkResult;
	
}
