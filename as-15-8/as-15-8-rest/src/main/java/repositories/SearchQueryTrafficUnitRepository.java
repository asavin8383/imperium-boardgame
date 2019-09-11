package repositories;

import model.traffic.SearchQueryTrafficUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchQueryTrafficUnitRepository extends JpaRepository<SearchQueryTrafficUnit, Long> {
}
