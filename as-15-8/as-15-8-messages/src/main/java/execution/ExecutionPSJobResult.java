package execution;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
public class ExecutionPSJobResult extends ExecutionJobResult {

	private boolean checkResult;
	
}
