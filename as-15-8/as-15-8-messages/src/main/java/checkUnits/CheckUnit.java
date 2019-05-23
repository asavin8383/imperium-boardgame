package checkUnits;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Проверяемая единица ЕРДИ
 * @author shabalinAI
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckUnit {

	/** Тип проверяемой единицы */
	private CheckUnitType type;
	
	/** Значение проверяемой единицы */
	private String value;
	
}
