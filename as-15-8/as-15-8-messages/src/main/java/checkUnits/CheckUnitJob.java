package checkUnits;

import enums.AccessToolParameters;
import enums.AccessToolUnit;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

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

	/** Набор параметров ПС/ПАСД*/
	@Getter
	private final Map<AccessToolParameters, String> accessToolParameters = new HashMap<>();
}
