package services.schedule;

import checkUnits.CheckMethod;
import checkUnits.CheckUnitType;
import lombok.RequiredArgsConstructor;
import model.catalog.AccessTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.PlannedProcessingTimeRepo;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PlannedProcessingTimeService {

    private final PlannedProcessingTimeRepo plannedProcessingTimeRepo;

    private Map<AccessTool, Map<CheckMethod, Long>> processingTimes = new HashMap<>();

    @PostConstruct
    public void loadTimes(){
        plannedProcessingTimeRepo.findAll()
                .forEach(processingTime -> {
                    if(processingTimes.containsKey(processingTime.getAccessTool())){
                        Map<CheckMethod, Long> methodTimes = processingTimes.get(processingTime.getAccessTool());
                        methodTimes.put(processingTime.getCheckMethod(), processingTime.getPlannedProcessingTimeMs());
                    } else {
                        Map<CheckMethod, Long> methodTimes = new HashMap<>();
                        methodTimes.put(processingTime.getCheckMethod(), processingTime.getPlannedProcessingTimeMs());
                        processingTimes.put(processingTime.getAccessTool(), methodTimes);
                    }
                });

    }

    public long getProcessingTime(AccessTool accessTool, CheckUnitType checkUnitType){
        if(processingTimes.containsKey(accessTool)){
            if(processingTimes.get(accessTool).containsKey(checkUnitType.getCheckMethod()))
                return processingTimes.get(accessTool).get(checkUnitType.getCheckMethod()) / 1000;
        }
        return 1;
    }
}
