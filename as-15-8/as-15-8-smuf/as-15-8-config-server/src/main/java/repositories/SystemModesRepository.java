package repositories;

import enums.SystemModeUnit;
import model.SystemMode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface SystemModesRepository  extends JpaRepository<SystemMode, Long> {

    @Query("select m from SystemMode m " +
            "join SystemModeCurrent mc " +
            "on mc.systemModeCurrent = m.id ")
    Optional<SystemMode> getCurrentSystemMode();

    @Transactional
    @Modifying
    @Query("update SystemModeCurrent smc " +
            "set smc.systemModeCurrent = :id")
    void setCurrentSystemMode(@Param("id") Long id);

    @Query("select m from SystemMode m where m.systemMode =:systemMode")
    Optional<SystemMode> findBySystemMode(@Param("systemMode") SystemModeUnit systemModeUnit);

}
