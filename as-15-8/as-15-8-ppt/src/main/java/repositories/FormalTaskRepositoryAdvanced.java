package repositories;

import model.enums.ExecutionStatus;
import model.task.FormalTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface FormalTaskRepositoryAdvanced {

	Page<FormalTask> findPage(List<ExecutionStatus> statuses, Long id, String operator, String fgisId, Pageable pageable);

}
