package repositories;

import model.task.Arrangement;
import model.task.ArrangementItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Creation date: 23.05.2019
 * Author: asavin
 */
public interface ArrangementItemRepository extends JpaRepository<ArrangementItem, Long> {

    @Query("SELECT item FROM ArrangementItem item WHERE item.arrangement.id=:arrangementId")
    List<ArrangementItem> findAllByArrangementId(@Param("arrangementId") Long arrangementId);

}
