package repositories;

import model.sor.SearchSystem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SearchSystemRepository extends JpaRepository<SearchSystem, Integer> {
}
