package repositories;

import model.task.ArrangementItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Creation date: 23.05.2019
 * Author: asavin
 */
public interface ArrangementItemRepositoryAdvanced {

    Page<ArrangementItem> findPage(Long arrangementId, Long id, Pageable pageable);
}
