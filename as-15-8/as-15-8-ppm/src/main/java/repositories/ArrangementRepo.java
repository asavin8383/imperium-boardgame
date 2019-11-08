package repositories;

import model.Arrangement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by san
 * Date: 31.10.2019
 */
@Repository
public interface ArrangementRepo extends JpaRepository<Arrangement, Long> {

    @Query("select DISTINCT a " +
            "from Arrangement a " +
            "join a.schedulePeriodArrangements spa " +
            "join spa.schedulePeriod sp " +
            "where sp.schedule.id = :id")
    List<Arrangement> findAllBySchedule(@Param("id") Long scheduleId);

    @Query("select DISTINCT a " +
            "from Arrangement a " +
            "left join a.schedulePeriodArrangements spa " +
            "where spa.id is null")
    Page<Arrangement> findAllAvailableArrangements(Pageable pageable);

    @Query("select DISTINCT a " +
            "from Arrangement a " +
            "left join a.schedulePeriodArrangements spa " +
            "where spa.id is null")
    List<Arrangement> findAllAvailableArrangements();
}
