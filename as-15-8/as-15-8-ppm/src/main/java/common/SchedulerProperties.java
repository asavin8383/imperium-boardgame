package common;

import checkUnits.CheckMethod;
import checkUnits.CheckUnitType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import robots.SlaPeriod;
import robots.SlaType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Data
@ConfigurationProperties
public class SchedulerProperties {

    private Integer totalWorkersCount;

    private final Map<String, String> accessTools = new HashMap<>();

    private Map<String, Map<CheckMethod, Long>> processingTimes = new HashMap<>();
    private Map<String, Map<String, Map<String, String>>> robotsSla = new HashMap<>();
    private Map<String, String> robotsTraffic = new HashMap<>();

    public long getProcessingTime(String accessTool, CheckUnitType checkUnitType){
        if(processingTimes.containsKey(accessTool)){
            if(processingTimes.get(accessTool).containsKey(checkUnitType.getCheckMethod()))
                return processingTimes.get(accessTool).get(checkUnitType.getCheckMethod()) / 1000;
        }
        return 1;
    }

    public Optional<Long> getRobotSlaCheckUnitsPerDay(SlaType slaType, String accessTool) {
        return getRobotSlaCheckUnitLimit(slaType, SlaPeriod.DAY, accessTool);
    }

    public Optional<Long> getRobotSlaCheckUnitsPerMonth(SlaType slaType, String accessTool) {
        return getRobotSlaCheckUnitLimit(slaType, SlaPeriod.MONTH, accessTool);
    }

    private Optional<Long> getRobotSlaCheckUnitLimit(SlaType slaType, SlaPeriod slaPeriod, String accessTool){
        try {
            Map<String, String> result = robotsSla.get(slaType.propertyKey()).get(accessTool);
            String res = result.get(slaPeriod.propertyKey());
            return Optional.of(Long.valueOf(res));
        } catch (Exception  e) {
            if (!(e instanceof NullPointerException))
                log.warn("Ошибка извлечения конфига robots_sla", e);
            return Optional.empty();
        }
    }

    public Optional<Long> getRobotTrafficPerCheckUnit(String accessTool){
        try {
            String traffic = robotsTraffic.get(accessTool);
            return Optional.of(Long.valueOf(traffic));
        } catch (Exception e) {
            if (!(e instanceof NullPointerException))
                log.warn("Ошибка извлечения конфига processing_time.traffic per_check_unit", e);
            return Optional.empty();
        }
    }

}
