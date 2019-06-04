package repositories;

import model.erdi.ERDI;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Creation date: 23.05.2019
 * Author: asavin
 */
public interface ERDIRepository extends JpaRepository<ERDI, Long>, ERDIRepositoryAdvanced {

}
