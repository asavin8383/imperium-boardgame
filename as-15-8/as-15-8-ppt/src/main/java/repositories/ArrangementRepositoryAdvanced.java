package repositories;

import enums.ExecutionStatus;
import model.task.Arrangement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Creation date: 22.05.2019
 * Author: asavin
 */
public interface ArrangementRepositoryAdvanced {

    Page<Arrangement> findPage(Long formalTaskId, Long id, Pageable pageable);

    Page<Arrangement> findPageByStatus(ExecutionStatus status, Pageable pageable);

    Page<Arrangement> findPageFiltered(List<ExecutionStatus> statuses, String operator, String fgisId, Pageable pageable);
}
