package execution;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
public class ExecutionAnonymizerResult extends ExecutionJobResult {

    private String errorCode;
    private String pageContent;

    private Boolean useEtalon;

    private String etalonErrorCode;
    private String etalonPageContent;

    private String finalUrl;
    private String stubUrl;

    public boolean hasError() {
        return errorCode != null;
    }

    public boolean hasEtalonError() {
        return etalonErrorCode != null;
    }

}

