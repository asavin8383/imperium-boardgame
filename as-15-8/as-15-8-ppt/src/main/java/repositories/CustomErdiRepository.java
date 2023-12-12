package repositories;

import model.traffic.CustomErdi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomErdiRepository extends JpaRepository<CustomErdi, Long> {

    Optional<CustomErdi> findByName(String name);
}
