package execution;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
public class ExecutionPureChannelJobResult extends ExecutionJobResult{
    private Integer httpStatus;
    private String httpHeaders;
    private String chromeErrorCode;
    private Boolean responseError;
    private String finalUrlPage;
    private String pageContent;

    public boolean hasError() {
        return chromeErrorCode != null;
    }
}
