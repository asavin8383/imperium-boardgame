package execution;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper=true)
@ToString(callSuper=true)
@NoArgsConstructor
public class NmapExecutionResult extends ExecutionJobResult {

    private String nmapLog;
    private Set<String> availableHosts = new HashSet<>();

}
