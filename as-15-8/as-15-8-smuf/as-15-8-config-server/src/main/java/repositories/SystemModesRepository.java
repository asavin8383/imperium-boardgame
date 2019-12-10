package repositories;

import model.SystemMode;
import model.SystemModeUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SystemModesRepository  extends JpaRepository<SystemMode, Long> {

    @Query("select m.systemMode from SystemMode m where m.active = true")
    Optional<SystemModeUnit> getCurrentMode();

    Optional<SystemMode> findBySystemMode(SystemModeUnit systemMode);
}
