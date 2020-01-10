package execution;

import checkUnits.CheckUnit;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import lombok.Data;

/**
 * Результат выполнения задания на проверку запрещенного ресурса
 * @author shabalinAI
 *
 */
@Data
@JsonTypeInfo(include=As.WRAPPER_OBJECT, use=Id.CLASS)
public abstract class ExecutionJobResult {
	
	/** ПС/ПАСД */
	private String accessTool;
	
	/** Единица ЕРДИ для проверки */
	private CheckUnit checkUnit;
	
    /**Скриншот полученной при проверке страницы*/
    private byte[] screenshot;

	/**Эталонноый скриншот, полученной при проверке страницы с выделенного прокси*/
	private byte[] etalonScreenshot;
}
