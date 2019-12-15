package execution;

import enums.CheckUnitJobResult;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
@NoArgsConstructor
public class ExecutionPSJobResult extends ExecutionJobResult {

	private boolean linkFound;

	private boolean error;
	private String errorDetails;
	private CheckUnitJobResult checkUnitJobResult;
	private List<String> urls;

}
