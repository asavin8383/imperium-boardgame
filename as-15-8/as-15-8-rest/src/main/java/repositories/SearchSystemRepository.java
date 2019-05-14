package repositories;

import model.catalog.AccessTool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchSystemRepository extends JpaRepository<AccessTool, Long> {

}
