package repositories;

import enums.AccessToolParameter;
import model.parameters.GlobalParameter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Creation date: 03.06.2019
 * Author: asavin
 */
@Repository
public interface GlobalParametersRepository extends JpaRepository<GlobalParameter, AccessToolParameter> {
}
