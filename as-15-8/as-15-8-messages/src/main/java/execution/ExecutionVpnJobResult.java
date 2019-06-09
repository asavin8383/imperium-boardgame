package execution;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@EqualsAndHashCode(callSuper=true)
@NoArgsConstructor
public class ExecutionVpnJobResult extends ExecutionJobResult{
    private Integer httpStatus;
    private String chromeErrorCode;
    private Boolean responseError;
    private String finalUrlPage;
    private String finalUrlPageEtalon;
    private String pageContent;
    private String pageContentEtalon;
    private String chromeErrorCodeEtalon;
    private String stubUrl;
}
