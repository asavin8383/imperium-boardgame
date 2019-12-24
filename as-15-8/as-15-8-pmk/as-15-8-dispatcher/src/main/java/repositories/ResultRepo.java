package repositories;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import checkUnits.CheckUnitType;
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
public interface ResultRepo extends JpaRepository<Result, Long>, ResultRepoAdvanced {

	@Query("select r from Result r " +
			"where r.arrangementId = :arrangementId and " +
			"r.erdiId = :erdiId and " +
			"r.checkUnitType = :checkUnitType and " +
			"r.checkUnitValue = :checkUnitValue")
	Optional<Result> findExisting(
			@Param("arrangementId") Long arrangementId,
			@Param("erdiId") Long erdiId,
			@Param("checkUnitType") CheckUnitType checkUnitType,
			@Param("checkUnitValue") String checkUnitValue);

	@Query("SELECT count(res) FROM Result res WHERE res.arrangementId = :id AND (res.result IS NULL OR res.result IN :results)")
	Long countByResultNullOrResultIn(@Param("id") Long id, @Param("results") List<CheckUnitJobResult> results);

	List<Result> findAllByArrangementId(Long id);

	@Query("select r from Result r " +
			"where r.arrangementId = :arrangementId and " +
			"r.result in(:results)")
	List<Result> findResultsForAct(@Param("arrangementId") Long arrangementId, @Param("results") Collection<CheckUnitJobResult> results);

	@Query("select r from Result r " +
			"where r.arrangementId = :arrangementId and " +
			"r.result in(:results)")
	List<Result> findResultsForAct(@Param("arrangementId") Long arrangementId, @Param("results") Collection<CheckUnitJobResult> results, Pageable pageable);

	@Query("select r from Result r " +
			"where r.arrangementId = :arrangementId and " +
			"r.result in(:results) and " +
			"r.checkForAct = true")
	List<Result> findCheckedResultsForAct(@Param("arrangementId") Long arrangementId, @Param("results") Collection<CheckUnitJobResult> results);

	Page<Result> findAllByArrangementId(Long id, Pageable pageable);
	@Query("select DISTINCT r from Result r" +
			" where r.arrangementId = :arr_id and" +
			" (r.checkUnitValue LIKE CONCAT('%',:query,'%') or r.checkUnitType LIKE CONCAT('%',:query,'%') or r.result LIKE CONCAT('%',:query,'%'))")
	Page<Result> findAllByArrangementAndQuery(@Param("arr_id") Long arrangementId, @Param("query")String query, Pageable pageable);


	@Query(value = "select max(res.endDate) from Result res where res.arrangementId = :id")
	LocalDateTime getMaxDateByArrangementId(@Param("id") Long id);

	@Query(value = "select min(res.startDate) from Result res where res.arrangementId = :id")
	LocalDateTime getMinDateByArrangementId(@Param("id") Long id);

	@Query("select " +
				"(sum(case when result in ('PLANNED', 'RUNNING') then 0 else 1 end) * 1.0 " +
				"/ "+
				"count(r.id)) * 100 as percent " +
			"from Result r " +
			"where arrangementId = :id")
	Optional<Integer> getCompletionPercent(@Param("id") Long id);

	@Query("select DISTINCT(r.checkUnitType) from Result r where r.arrangementId=:arrangement_id")
	List<CheckUnitType> getCheckUnitTypesByArrangementId(@Param("arrangement_id") Long arrangementId);

	@Query("select DISTINCT(r.result) from Result r where r.arrangementId=:arrangement_id")
	List<CheckUnitJobResult> getCheckUnitJobResultsByArrangementId(@Param("arrangement_id") Long arrangementId);


	@Query("SELECT count(res) FROM Result res WHERE res.arrangementId = :id AND (res.result NOT IN ('PLANNED', 'RUNNING'))")
	Integer getNotRunningNotPlanned(@Param("id") Long id);

	@Query("SELECT count(res) FROM Result res WHERE res.arrangementId = :id AND (res.result IS NULL OR res.result NOT IN :results)")
	Long countByNotResultIn(@Param("id") Long id, @Param("results") List<CheckUnitJobResult> results);
}
