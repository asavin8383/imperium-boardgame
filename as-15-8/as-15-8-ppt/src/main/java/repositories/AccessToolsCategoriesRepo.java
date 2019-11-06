package repositories;

import model.catalog.AccessToolsCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Creation date: 06.09.2019
 * Author: asavin
 */
@Repository
public interface AccessToolsCategoriesRepo extends JpaRepository<AccessToolsCategory, Long> {

    AccessToolsCategory findByName(String name);

    AccessToolsCategory findOneByOrderByIdDesc();

}
