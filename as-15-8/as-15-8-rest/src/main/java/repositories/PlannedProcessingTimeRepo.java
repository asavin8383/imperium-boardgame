package repositories;

import model.schedule.PlannedProcessingTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Creation date: 08.08.2019
 * Author: asavin
 */
@Repository
public interface PlannedProcessingTimeRepo extends JpaRepository<PlannedProcessingTime,Long> {

    List<PlannedProcessingTime> findAllByAccessTool(Long accessToolId);
}
