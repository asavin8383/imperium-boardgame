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

	/** Идентификатор мероприятия */
	private Long arrangementID;
	
	/** ПС/ПАСД */
	private AccessToolUnit accessToolUnit;
	
	/** Идентификатор ЕРДИ */
	private Long erdiID;
	
	/** Проверяемый ресурс */
	private CheckUnit checkUnit;
}
