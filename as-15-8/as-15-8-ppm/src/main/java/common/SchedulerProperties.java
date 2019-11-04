package common;

import checkUnits.CheckMethod;
import checkUnits.CheckUnitType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Data
@ConfigurationProperties
public class SchedulerProperties {

    private Long totalWorkersCount;

    private final Map<String, String> accessTools = new HashMap<>();

    private Map<String, Map<CheckMethod, Long>> processingTimes = new HashMap<>();

    public long getProcessingTime(String accessTool, CheckUnitType checkUnitType){
        if(processingTimes.containsKey(accessTool)){
            if(processingTimes.get(accessTool).containsKey(checkUnitType.getCheckMethod()))
                return processingTimes.get(accessTool).get(checkUnitType.getCheckMethod()) / 1000;
        }
        return 1;
    }
}
