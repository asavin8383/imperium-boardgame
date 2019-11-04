package repositories;

import model.traffic.TrafficBriefView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TrafficRepositoryCustom {

    Page<TrafficBriefView> findAllTrafficInfo(String query, Pageable pageable);

}
