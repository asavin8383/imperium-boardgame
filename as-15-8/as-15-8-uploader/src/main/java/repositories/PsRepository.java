package repositories;

import model.scheme.PsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PsRepository extends JpaRepository<PsRecord, Integer> {
}
