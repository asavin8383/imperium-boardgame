package repositories;

import model.scheme.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Long>, MissionRepositoryCustom{

    List<Mission> findByOrigIdIn(List<String> ids);

    Mission findByOrigId(String originId);

    @Query(value = "select m.origId from Mission m where m.id = :id")
    Long getOriginId(@Param("id") Long id);
}
