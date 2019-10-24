package repositories;

import model.scheme.Mission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MissionRepository extends JpaRepository<Mission, Integer>, MissionRepositoryCustom{

    List<Mission> findByOrigIdIn(List<String> ids);

    Mission findByOrigId(String originId);

}
