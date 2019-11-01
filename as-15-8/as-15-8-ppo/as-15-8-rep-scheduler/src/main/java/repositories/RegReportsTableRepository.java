package repositories;

import model.RegReportsTable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * User: asinjavin
 * Date: 31.10.2019
 * Time: 18:43
 */
public interface RegReportsTableRepository extends JpaRepository<RegReportsTable, Long>
{
    List<RegReportsTable> findAllByRepTpId(long rep_tp_id);
}
