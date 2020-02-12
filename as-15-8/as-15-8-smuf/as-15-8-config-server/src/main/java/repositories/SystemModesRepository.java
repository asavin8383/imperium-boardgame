package repositories;

import enums.SystemModeUnit;
import model.SystemMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface SystemModesRepository  extends JpaRepository<SystemMode, Long> {

    @Query("select m from SystemMode m where m.active = true")
    Optional<SystemMode> getCurrentSystemMode();

    @Query("select m from SystemMode m where m.systemMode =:systemMode")
    Optional<SystemMode> findBySystemMode(@Param("systemMode") SystemModeUnit systemModeUnit);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update SystemMode s set s.active=:enabled")
    void setAllSysemModesEnabled(@Param("enabled") Boolean enabled);

    @Query("select m from SystemMode m")
    Optional<List<SystemMode>> findALL();
}
