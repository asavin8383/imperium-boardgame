package repositories;

import model.scheme.Subtype;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubtypeRepository extends JpaRepository<Subtype, Integer> {
}
