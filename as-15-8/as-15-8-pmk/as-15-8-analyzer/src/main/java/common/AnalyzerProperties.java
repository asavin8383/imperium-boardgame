package common;

import enums.AccessToolParameter;
import enums.AccessToolUnit;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by san
 * Date: 26.02.2020
 */
@Slf4j
@Data
@ConfigurationProperties
public class AnalyzerProperties {

    private final Map<String, Map<String, Map<String, String>>> robots = new HashMap<>();

    @Getter
    private AccessToolUnits props;

    @PostConstruct
    private void loadProps() {

        this.props = new AccessToolUnits();
        robots.forEach((accessToolUnitString, accessToolUnitPropsMap) ->{
            AccessToolUnit accessToolUnit = AccessToolUnit.fromPropertyKey(accessToolUnitString);
            AccessToolUnitProps accessToolUnitProps = new AccessToolUnitProps();
            accessToolUnitPropsMap.forEach((robotName, robotPropsMap) -> {
                RobotProps robotProps = new RobotProps();
                robotPropsMap.forEach((propName, propValue) -> {
                    AccessToolParameter accessToolParameter = AccessToolParameter.fromPropertyKey(propName);
                    robotProps.getProps().put(accessToolParameter, propValue);
                });
                accessToolUnitProps.getRobotProps().put(robotName, robotProps);
            });
            props.getAccessToolUnits().put(accessToolUnit, accessToolUnitProps);
        });
    }

    public Optional<AccessToolUnit> getAccessToolUnit(String accessTool){
        return props.getAccessToolUnits().entrySet()
            .stream()
            .filter(accessToolUnitProps ->
                accessToolUnitProps.getValue().getRobotProps().keySet()
                    .stream().anyMatch(curAccessTool -> curAccessTool.toLowerCase().equals(accessTool.toLowerCase()))
            )
            .map(Map.Entry::getKey)
            .findFirst();
    }

    @Data
    public static class AccessToolUnits{
        private Map<AccessToolUnit, AccessToolUnitProps> accessToolUnits = new HashMap<>();
    }

    @Data
    public static class AccessToolUnitProps{
        private Map<String, RobotProps> robotProps = new HashMap<>();
    }

    @Data
    public static class RobotProps{
        private Map<AccessToolParameter, String> props = new HashMap<>();
    }

}
