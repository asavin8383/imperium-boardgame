package robots;

import common.ExecutorProperties;
import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ChromeSettings {

    private static ExecutorProperties.ChromeProperties chromeProperties;

    static {
        chromeProperties = ExecutorProperties.getChromeProperties();
    }

    public class Extension {

        private String stringId;
        private String version;
        private String popup;

        public Extension(String stringId, String version, String popup) {
            this.stringId = stringId;
            this.version = version;
            this.popup = popup;
        }

        public String getStringId() {
            return stringId;
        }

        public String getVersion() {
            return version;
        }

        public String getPopupUrl() {
            return "chrome-extension://" + stringId + popup;
        }
    }

    public static String buildLoadExtensionArgValue(List<Extension> extensions) {
        return extensions.stream()
                .map(ChromeSettings::buildExtensionPath)
                .collect(Collectors.joining(","));
    }

    public static String buildExtensionPath(Extension ext) {
        return new StringBuilder()
                .append(chromeProperties.getUserDataDir()).append("/")
                .append(chromeProperties.getProfileName()).append("/")
                .append(chromeProperties.getExtensionsDir()).append("/")
                .append(ext.stringId).append("/")
                .append(ext.version).toString();
    }

}
