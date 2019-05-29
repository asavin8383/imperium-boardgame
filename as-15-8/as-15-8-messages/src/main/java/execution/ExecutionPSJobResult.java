package execution;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
@NoArgsConstructor
public class ExecutionPSJobResult extends ExecutionJobResult {

	private boolean linkFound;

}
