package repositories;

import model.DetailResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface DetailResultRepo extends JpaRepository<DetailResult, Long> {
}
