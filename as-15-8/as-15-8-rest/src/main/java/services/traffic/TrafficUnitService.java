package services.traffic;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.ErdiTrafficUnitRepository;
import repositories.SearchQueryTrafficUnitRepository;
import repositories.TrafficUnitRepository;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class TrafficUnitService {

    private final TrafficUnitRepository trafficUnitRepository;

    private final ErdiTrafficUnitRepository erdiTrafficUnitRepository;

    private final SearchQueryTrafficUnitRepository searchQueryTrafficUnitRepository;



}
