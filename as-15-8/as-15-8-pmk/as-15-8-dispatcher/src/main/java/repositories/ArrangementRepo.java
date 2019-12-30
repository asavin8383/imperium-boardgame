package repositories;

import model.Arrangement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArrangementRepo extends JpaRepository<Arrangement, Long> {

    @Query("select a from Arrangement a " +
            "where a.status = 'RUNNING'")
    List<Arrangement> findRunning();
}
