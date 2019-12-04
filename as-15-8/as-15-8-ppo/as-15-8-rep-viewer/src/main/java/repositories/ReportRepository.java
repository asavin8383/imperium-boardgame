package repositories;

import model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * User: asinjavin
 * Date: 22.11.2019
 * Time: 4:49
 */
public interface ReportRepository extends JpaRepository<Report, Long>
{
}
