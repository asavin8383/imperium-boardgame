package execution;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;

import checkUnits.CheckUnit;
import enums.AccessToolUnit;
import lombok.Data;

/**
 * Результат выполнения задания на проверку запрещенного ресурса
 * @author shabalinAI
 *
 */
@Data
@JsonTypeInfo(include=As.WRAPPER_OBJECT, use=Id.CLASS)
public abstract class ExecutionJobResult {

	/** Идентификатор задания */
	private Long jobID;
	
	/** ПС/ПАСД */
	private AccessToolUnit accessToolUnit;
	
	/** Единица ЕРДИ для проверки */
	private CheckUnit checkUnit;
	
    /**Скриншот полученной при проверке страницы*/
    private byte[] screenshot;
}
