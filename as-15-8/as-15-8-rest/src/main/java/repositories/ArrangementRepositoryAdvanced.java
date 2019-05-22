package repositories;

import model.task.Arrangement;
import model.task.FormalTask;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Creation date: 22.05.2019
 * Author: asavin
 */
public interface ArrangementRepositoryAdvanced {

    Page<Arrangement> findPage(Long formalTaskId, Long id, Pageable pageable);
}
