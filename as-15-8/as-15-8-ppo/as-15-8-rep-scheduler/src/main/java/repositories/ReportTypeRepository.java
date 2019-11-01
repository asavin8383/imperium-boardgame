package repositories;

import model.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * User: asinjavin
 * Date: 31.10.2019
 * Time: 18:43
 */
public interface ReportTypeRepository extends JpaRepository<ReportType, Long>
{
}
