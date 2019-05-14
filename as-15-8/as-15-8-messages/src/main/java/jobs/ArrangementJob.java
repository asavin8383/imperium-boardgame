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

	private Long id;
	
	private AccessToolUnit accessToolUnit;
	
	@Getter
	private List<ERDIJob> erdiJobList = new ArrayList<>();
	
	public void addERDIJob(ERDIJob erdiJob) {
		this.erdiJobList.add(erdiJob);
	}
	
}
