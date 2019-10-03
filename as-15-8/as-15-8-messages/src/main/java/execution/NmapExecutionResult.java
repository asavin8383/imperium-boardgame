package execution;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
@NoArgsConstructor
public class NmapExecutionResult extends ExecutionJobResult {

    private String nmapLogs;
    private Map<String, Set<Long>> openedPorts = new HashMap<>();

}
