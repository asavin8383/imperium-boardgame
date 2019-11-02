package repositories;

import model.traffic.TrafficView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TrafficRepositoryCustom {

    Page<TrafficView> findAllTrafficInfo(String query, Pageable pageable);

}
