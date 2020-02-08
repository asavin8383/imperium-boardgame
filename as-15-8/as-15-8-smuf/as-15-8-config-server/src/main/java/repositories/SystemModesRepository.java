package repositories;

import model.SystemMode;
import enums.SystemModeUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SystemModesRepository  extends JpaRepository<SystemMode, Long> {

    @Query("select m from SystemMode m where m.active = true")
    Optional<SystemMode> getCurrentSystemMode();

    @Query("select m from SystemMode m where m.systemMode =:systemMode")
    Optional<SystemMode> findBySystemMode(@Param("systemMode") SystemModeUnit systemMode);

    @Query("select m from SystemMode m")
    Optional<List<SystemMode>> findALL();
}
