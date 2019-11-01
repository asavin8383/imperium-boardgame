package repositories;

import model.ReportStat;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * User: asinjavin
 * Date: 31.10.2019
 * Time: 18:43
 */
public interface ReportStatRepository extends JpaRepository<ReportStat, Long>
{
}
