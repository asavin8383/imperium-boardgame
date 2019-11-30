package repositories;

import model.traffic.ErdiTrafficUnit;
import model.traffic.ErdiTrafficUnitContent;
import model.traffic.TrafficUnit;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErdiContentJoinRepository extends JpaRepository<ErdiTrafficUnitContent, Long> {

    List<ErdiTrafficUnitContent> findAllByTrafficUnitAndErdiIdIn(ErdiTrafficUnit unit, List<Long> formalIds);

    Page<ErdiTrafficUnitContent> findByTrafficUnit(TrafficUnit trafficUnit, Pageable pageable);

}
