package repositories;

import model.ReportAdminTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * User: asinjavin
 * Date: 31.10.2019
 * Time: 18:43
 */
public interface ReportAdminTableRepository extends JpaRepository<ReportAdminTable, Long>
{
    List<ReportAdminTable> findByRepTpId(long rep_tp_id);
}
