package repositories;

import arrangement.ArrangementToExecution;
import model.Arrangement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArrangementRepo extends JpaRepository<Arrangement, Long> {

}
