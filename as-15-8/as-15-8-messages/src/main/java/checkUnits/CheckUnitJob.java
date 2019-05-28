package checkUnits;

import enums.AccessToolUnit;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Задание на проверку запрещенного ресурса
 * @author shabalinAI
 *
 */
@Data
@NoArgsConstructor
public class CheckUnitJob {

	/** Идентификатор задания */
	private Long jobID;
	
	/** ПС/ПАСД */
	private AccessToolUnit accessToolUnit;
	
	/** Проверяемый ресурс */
	private CheckUnit checkUnit;
}
