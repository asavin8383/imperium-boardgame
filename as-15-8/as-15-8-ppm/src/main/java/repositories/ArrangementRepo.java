package repositories;

import model.Arrangement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by san
 * Date: 31.10.2019
 */
@Repository
public interface ArrangementRepo extends JpaRepository<Arrangement, Long> {
}
