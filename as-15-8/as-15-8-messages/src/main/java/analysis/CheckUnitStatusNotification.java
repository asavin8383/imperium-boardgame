package analysis;

import checkUnits.CheckUnit;
import enums.CheckUnitJobResult;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Date;

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
	public CheckUnitStatusNotification(CheckUnitJobResult checkResult, CheckUnit checkUnit, Date startTime, String description){
		super(checkResult, checkUnit, startTime);
		this.description = description;
	}
}
