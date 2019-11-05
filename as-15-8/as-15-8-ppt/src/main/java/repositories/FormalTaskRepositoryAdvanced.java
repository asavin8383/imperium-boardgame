package repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import model.task.FormalTask;

import java.util.List;

public interface FormalTaskRepositoryAdvanced {

	Page<FormalTask> findPage(Long id, String operator, Pageable pageable);
	List<?> getFormalTasksGroupingByStatus();
}
