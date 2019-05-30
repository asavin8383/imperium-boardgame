package checkUnits;

import enums.CheckUnitJobResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Сообщение об изменении статуса проверки запрещенного ресурса
 * @author shabalinAI
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckUnitStatusNotification {

	/** Идентифиатор задания */
	private Long jobID;
	
	/** Статус задания */
	private CheckUnitJobResult checkUnitStatus;
	
}
