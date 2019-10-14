package repositories;

import model.traffic.projection.TrafficProjection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TrafficRepositoryCustom {

    Page<TrafficProjection> findAllTrafficInfo(String query, Pageable pageable);

}
