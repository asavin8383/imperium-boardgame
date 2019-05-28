package analysis;

import checkUnits.CheckUnit;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

/**
 * Creation date: 27.05.2019
 * Author: asavin
 */

@Data
@JsonTypeInfo(include= JsonTypeInfo.As.WRAPPER_OBJECT, use= JsonTypeInfo.Id.CLASS)
public abstract class AnalysisResult {
    /** Идентификатор мероприятия */
    private Long arrangenmentID;

    /** Идентификатор ЕРДИ */
    private Long erdiID;

    /** Единица ЕРДИ для проверки */
    private CheckUnit checkUnit;

    /**Скриншот полученной при проверке страницы*/
    private byte[] screenshot;
}
