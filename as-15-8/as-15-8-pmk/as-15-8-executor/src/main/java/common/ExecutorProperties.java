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

    private ChromeProperties chrome;


    private static EtalonProperties etalonExtProperties;

    private static ChromeProperties chromeProperties;

    private static Long screenShotWaitTimeoutExt;

    private static URL seleniumHubUrlExt;

    @Getter
    private AccessToolUnits props;

    @PostConstruct
    private void loadProps() throws MalformedURLException {
        etalonExtProperties = etalon;
        seleniumHubUrlExt = new URL(seleniumHubUrl);
        chromeProperties = chrome;

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

    public static ChromeProperties getChromeProperties(){
        return chromeProperties;
    }

    public static URL getSeleniumHubUrl(){
        return seleniumHubUrlExt;
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

    @Data
    public static class EtalonProperties {

        private Boolean enabled;

        private Proxy proxy;

        @Data
        public static class Proxy {
            private String type;
            private String host;
            private String port;
            private String username;
            private String password;
        }
    }

    @Data
    public static class NmapProperties {
        private String path;
        private Boolean useProxy;
        private String[] portsToCheck;
    }

    @Data
    public static class ChromeProperties {
        private String userDataDir;
        private String extensionsDir;
        private String profileName;
    }
}
