package repositories;

import model.ParamReport;
import model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * User: asinjavin
 * Date: 31.10.2019
 * Time: 18:43
 */
public interface ParamReportRepository extends JpaRepository<ParamReport, Long>
{
    Optional<ParamReport> findByRepId(long repId);

    @Query(value = "select distinct s from ParamReport s " +
            "where s.repTpId = :repTpId "
            + "and ("
            + "concat(s.repId, '') like lower(concat('%',:query,'%')) "
            + "or lower(s.msr_prd_tp) like lower(concat('%',:query,'%')) "
            + "or lower(s.msr_prd_caption) like lower(concat('%',:query,'%')) "
            + "or lower(s.format) like lower(concat('%',:query,'%')) "
            + ") "
    )
    Page<ParamReport> findByRepTpIdAndQuery(@Param("repTpId") int repTpId, @Param("query") String query, Pageable pageable);

}
