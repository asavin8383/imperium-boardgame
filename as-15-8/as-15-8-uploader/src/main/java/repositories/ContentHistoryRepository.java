package repositories;

import model.scheme.ContentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ContentHistoryRepository extends JpaRepository<ContentHistory, Long> {
}
