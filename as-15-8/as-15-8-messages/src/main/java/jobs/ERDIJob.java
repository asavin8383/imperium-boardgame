package jobs;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Задание на проверку ЕРДИ
 * @author shabalinAI
 *
 */
@Data
@NoArgsConstructor
public class ERDIJob {

	/** Идентификатор ЕРДИ */
	private Long id;

	public ERDIJob(Long id) {
		this.id = id;
	}
}
