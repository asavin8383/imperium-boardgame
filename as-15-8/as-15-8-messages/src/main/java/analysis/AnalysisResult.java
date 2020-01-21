package analysis;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Результат анализа результатов выполнения задания на проверку запрещенного ресурса
 * Author: asavin
 */

@Data
@EqualsAndHashCode(callSuper = true)
public abstract class AnalysisResult extends CheckUnitResult {

    /**Скриншот полученной при проверке страницы*/
    private byte[] screenshot;

    /**Скриншот полученной при проверке страницы с выделенного прокси (эталона) */
    private byte[] etalonScreenshot;
}
