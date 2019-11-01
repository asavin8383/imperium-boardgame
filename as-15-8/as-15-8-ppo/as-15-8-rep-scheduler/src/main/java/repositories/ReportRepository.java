package repositories;

import model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * User: asinjavin
 * Date: 31.10.2019
 * Time: 18:43
 */
public interface ReportRepository extends JpaRepository<Report, Long>
{
    @Query(value = "SELECT r FROM Report r WHERE msr_prd_end_dttm < current_timestamp and status = 'NEW'")
    List<Report> getTodo();

    Optional<Report> findByRepIdAndFormat(long rep_id,String format);

    Optional<Report> findByRepId(long rep_id);
}
