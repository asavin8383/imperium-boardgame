package repositories;

import model.NmapDetailResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface NmapDetailResultRepo extends JpaRepository<NmapDetailResult, Long> {
}
