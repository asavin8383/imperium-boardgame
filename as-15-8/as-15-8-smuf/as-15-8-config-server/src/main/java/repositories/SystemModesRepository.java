package repositories;

import model.SystemMode;
import enums.SystemModeUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface SystemModesRepository  extends JpaRepository<SystemMode, Long> {

    @Query("select m from SystemMode m where m.active = true")
    Optional<SystemMode> getCurrentSystemMode();

    Optional<SystemMode> findBySystemMode(SystemModeUnit systemMode);

}
