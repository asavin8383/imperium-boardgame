package repositories;

import model.sor.Pasd;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PasdRepository extends JpaRepository<Pasd, Integer> {
}
