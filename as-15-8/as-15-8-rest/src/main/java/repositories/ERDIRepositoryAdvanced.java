package repositories;

import model.erdi.ERDI;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Creation date: 23.05.2019
 * Author: asavin
 */
public interface ERDIRepositoryAdvanced {

    Page<ERDI> findPage(Long id, Long arrangementId, String organization, String blocktype, Pageable pageable);
}
