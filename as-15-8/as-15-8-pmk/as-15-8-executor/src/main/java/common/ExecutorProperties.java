package common;

import enums.AccessToolParameter;
import enums.AccessToolUnit;
import lombok.Data;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Data
@ConfigurationProperties
public class ExecutorProperties {

    private final Map<String, Map<String, Map<String, String>>> robots = new HashMap<>();

    private EtalonProperties etalon;

    /** URL selenium хаба */
    private String seleniumHubUrl;

    private NmapProperties nmap;

    private ScreenshotProperties screenshot;

    private ExecutorProps executor;


    private static EtalonProperties etalonExtProperties;

    private static ScreenshotProperties screenshotProperties;

    private static URL seleniumHubUrlExt;

    @Getter
    private AccessToolUnits props;

    @PostConstruct
    private void loadProps() throws MalformedURLException {
        etalonExtProperties = etalon;
        seleniumHubUrlExt = new URL(seleniumHubUrl);
        screenshotProperties = screenshot;

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

    public static EtalonProperties getEtalon(){
        return etalonExtProperties;
    }

    public static URL getSeleniumHubUrl(){
        return seleniumHubUrlExt;
    }

    public static ScreenshotProperties getScreenshotProperties() { return screenshotProperties; }

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

    @Data
    public static class EtalonProperties {

        private Boolean enabled;

        private Proxy proxy;

        @Data
        public static class Proxy {
            private String type;
            private String host;
            private String port;
        }
    }

    @Data
    public static class NmapProperties {
        private String path;
        private Boolean useProxy;
        private String[] portsToCheck;
        private int subnetIpCount;
    }

    @Data
    public static class ScreenshotProperties{
        private String extId;
        private String extVersion;
        private String extPopup;
    }

    @Data
    public static class ExecutorProps{
        private Integer timeout;
    }
}
