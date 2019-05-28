package analysis;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
public class PS_AnalysisJobResult extends AnalysisResult {

	private boolean checkResult;
	
}
