package repositories;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import enums.CheckUnitJobResult;
import model.Result;


/**
 * Creation date: 27.05.2019
 * Author: asavin
 */

@Repository
public interface ResultRepo extends JpaRepository<Result, Long> {
	
	@Query("SELECT count(res) FROM Result res WHERE res.arrangementId = :id AND (res.result IS NULL OR res.result IN :results)")
	Long countByResultNullOrResultIn(@Param("id") Long id, @Param("results") List<CheckUnitJobResult> results);

	List<Result> findAllByArrangementId(Long id);
	List<Result> findByArrangementIdAndResultIn(Long arrangementId, Collection<CheckUnitJobResult> results);

	Page<Result> findAllByArrangementId(Long id, Pageable pageable);
	@Query("select DISTINCT r from Result r" +
			" where r.arrangementId = :arr_id and" +
			" (r.checkUnitValue LIKE CONCAT('%',:query,'%') or r.checkUnitType LIKE CONCAT('%',:query,'%') or r.result LIKE CONCAT('%',:query,'%'))")
	Page<Result> findAllByArrangementAndQuery(@Param("arr_id") Long arrangementId, @Param("query")String query, Pageable pageable);


	@Query(value = "select max(res.startDate) from Result res where res.arrangementId = :id")
	LocalDateTime getMaxDateByArrangementId(@Param("id") Long id);

	@Query(value = "select min(res.endDate) from Result res where res.arrangementId = :id")
	LocalDateTime getMinDateByArrangementId(@Param("id") Long id);

}
