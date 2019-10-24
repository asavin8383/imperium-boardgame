package repositories;

import model.scheme.Mission;
import model.scheme.PsRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface MissionRepositoryCustom {

    Page<Mission> findByQuery(String query, Pageable pageable);

}
