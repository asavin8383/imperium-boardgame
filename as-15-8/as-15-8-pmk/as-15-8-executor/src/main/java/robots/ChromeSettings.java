package robots;

import lombok.experimental.UtilityClass;

import java.util.List;
import java.util.stream.Collectors;

@UtilityClass
public class ChromeSettings {

    public static final String USER_DATA_FOLDER = "/home/selenium/chrome";

    public static final String EXTENSION_FOLDER = "Extensions";

    public static final String PROFILE_NAME = "Default";

    public enum Extension {

        NIMBUS("bpconcjcammlapcogcnnelfmaeghhagj", "8.9.5_0", "/popup.html"),
        GMAIL_SCREENSHOT("boepdnhlmfleonjnaoaemgcggppoikog", "1.0.0.21_0", "/popup.html"),
        HOLA("gkojfkhlekighikafcpjkiklfbnlmeio", "1.157.821_0", "/js/popup.html");

        private String stringId;
        private String version;
        private String popup;

        Extension(String stringId, String version, String popup) {
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
                .append(USER_DATA_FOLDER).append("/")
                .append(PROFILE_NAME).append("/")
                .append(EXTENSION_FOLDER).append("/")
                .append(ext.stringId).append("/")
                .append(ext.version).toString();
    }

    public static Extension getScreenshotExtension() {
        return Extension.GMAIL_SCREENSHOT;
    }

}
