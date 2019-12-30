package analysis;

import checkUnits.CheckUnit;
import enums.CheckUnitJobResult;
import lombok.*;

/**
 * Сообщение об изменении статуса проверки запрещенного ресурса
 * @author shabalinAI
 *
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CheckUnitStatusNotification extends CheckUnitResult {
	
	private String description;

	@Builder
	public CheckUnitStatusNotification(Long jobID, Long erdiID, CheckUnitJobResult checkResult, CheckUnit checkUnit, String description){
		super(jobID, erdiID, checkResult, checkUnit);
		this.description = description;
	}
}
