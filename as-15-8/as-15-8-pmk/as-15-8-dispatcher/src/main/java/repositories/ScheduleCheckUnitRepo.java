package repositories;

import model.ScheduleCheckUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
public interface ScheduleCheckUnitRepo extends JpaRepository<ScheduleCheckUnit, Long> {

    @Transactional
    Long deleteByArrangementIdAndErdiId(Long arrangementId, Long erdiId);
}
