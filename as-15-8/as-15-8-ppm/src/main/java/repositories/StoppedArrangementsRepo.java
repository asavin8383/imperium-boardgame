package repositories;

import model.StoppedArrangement;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoppedArrangementsRepo extends JpaRepository<StoppedArrangement, Long> {
}
