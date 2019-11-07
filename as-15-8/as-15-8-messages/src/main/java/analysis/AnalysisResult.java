package analysis;

import checkUnits.CheckUnit;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import enums.CheckUnitJobResult;
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

    /** Результат проверки **/
    private CheckUnitJobResult checkResult;

    /**Скриншот полученной при проверке страницы*/
    private byte[] screenshot;

    /**Скриншот полученной при проверке страницы с выделенного прокси (эталона) */
    private byte[] etalonScreenshot;
}
