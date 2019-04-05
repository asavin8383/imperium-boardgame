package repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import model.SearchSystem;

@Repository
public interface SearchSystemRepository extends JpaRepository<SearchSystem, Long> {

}
