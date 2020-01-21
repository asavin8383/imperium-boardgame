package checkUnits;

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

	/** ПС/ПАСД */
	private String accessTool;
	
	/** Проверяемый ресурс */
	private CheckUnit checkUnit;
}
