package repositories;

import model.scheme.PasdRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasdRepository extends JpaRepository<PasdRecord, Integer> {
}
