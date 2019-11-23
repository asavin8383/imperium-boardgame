package repositories;

import model.task.ClientNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by san
 * Date: 23.11.2019
 */
@Repository
public interface ClientNotificationRepo extends JpaRepository<ClientNotification, Long> {

    List<ClientNotification> findAllByOperatorAndViewed(String operator, boolean viewed);
}
