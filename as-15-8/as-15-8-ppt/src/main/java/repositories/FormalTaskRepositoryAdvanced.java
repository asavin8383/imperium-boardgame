package repositories;

import model.task.ExecutionStatusStatistics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import model.task.FormalTask;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FormalTaskRepositoryAdvanced {

	Page<FormalTask> findPage(Long id, String operator, Pageable pageable);

}
