package repositories;

import model.PsDetailResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface PsDetailResultRepo extends JpaRepository<PsDetailResult, Long> {
}
