package repositories;

import model.erdi.ERDI;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Creation date: 23.05.2019
 * Author: asavin
 */
@Repository
public interface ERDIRepository extends JpaRepository<ERDI, Long>, ERDIRepositoryAdvanced {

}
