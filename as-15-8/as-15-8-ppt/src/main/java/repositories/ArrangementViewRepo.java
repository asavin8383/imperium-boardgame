package repositories;

import model.task.ArrangementView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Creation date: 15.06.2019
 * Author: asavin
 */
@Repository
public interface ArrangementViewRepo extends JpaRepository<ArrangementView, Long> {

    List<ArrangementView> findAllByUserAndViewed(String user, boolean viewed);
}
