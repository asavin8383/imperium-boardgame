package repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import model.ArrangementResult;

@Repository
public interface ArrangementResultRepository extends JpaRepository<ArrangementResult, Long>{

}
