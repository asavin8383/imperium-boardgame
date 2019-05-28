package analysis;

import checkUnits.CheckUnit;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
 * Результат анализа результатов выполнения задания на проверку запрещенного ресурса
 * Author: asavin
 */

@Data
@JsonTypeInfo(include= JsonTypeInfo.As.WRAPPER_OBJECT, use= JsonTypeInfo.Id.CLASS)
public abstract class AnalysisResult {
	
    /** Идентификатор задания */
    private Long jobID;

    /** Единица ЕРДИ для проверки */
    private CheckUnit checkUnit;

    /**Скриншот полученной при проверке страницы*/
    private byte[] screenshot;
}
