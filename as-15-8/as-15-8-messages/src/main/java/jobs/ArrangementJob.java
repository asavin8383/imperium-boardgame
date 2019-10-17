package jobs;

import enums.AccessToolParameter;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Задание на проверку мероприятия
 * @author shabalinAI
 *
 */
@Data
@NoArgsConstructor
public class ArrangementJob {

	/**Тип запуска мероприятия*/
	public enum JobRunType{START, RESTART}

	/** Идентификатор мероприятия */
	private Long id;

	/**Тип запуска мероприятия, по умолчанию - START*/
	private JobRunType runType = JobRunType.START;
	
	/** ПС/ПАСД */
	private String accessTool;

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

	/** Набор параметров ПС/ПАСД*/
	@Getter
	private final Map<AccessToolParameter, String> accessToolParameters = new HashMap<>();
	
}
