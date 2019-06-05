package repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import enums.CheckUnitJobResult;
import model.ArrangementResult;

/**
 * Creation date: 27.05.2019
 * Author: asavin
 */

@Repository
public interface ArrangementResultRepository extends JpaRepository<ArrangementResult, Long> {
	
	@Query("SELECT count(res) FROM ArrangementResult res WHERE res.id = :id and res.result IS NULL OR res.result IN :results")
	Long countByResultNullOrResultIn(@Param("id") Long id, @Param("results") List<CheckUnitJobResult> results);
	
}
