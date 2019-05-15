package jobs;

import java.util.ArrayList;
import java.util.List;

import enums.AccessToolUnit;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Задание на проверку мероприятия
 * @author shabalinAI
 *
 */
@Data
@NoArgsConstructor
public class ArrangementJob {

	/** Идентификатор мероприятия */
	private Long id;
	
	/** ПС/ПАСД */
	private AccessToolUnit accessToolUnit;
	
	/** Список заданий на проверку ЕРДИ */
	@Getter
	private List<ERDIJob> erdiJobList = new ArrayList<>();
	
	/**
	 * Метод добавления задания на проверку ЕРДИ
	 * @param erdiJob Задание на проверку ЕРДИ
	 */
	public void addERDIJob(ERDIJob erdiJob) {
		this.erdiJobList.add(erdiJob);
	}
	
}
