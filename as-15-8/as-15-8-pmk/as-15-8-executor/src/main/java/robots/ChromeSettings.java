package robots;

import common.ExecutorProperties;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ChromeSettings {

    private static ExecutorProperties.ScreenshotProperties screenshotProperties;

    static {
        screenshotProperties = ExecutorProperties.getScreenshotProperties();
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

    public static Extension getScreenshotExtension(){
        return new Extension(
                screenshotProperties.getExtId(),
                screenshotProperties.getExtVersion(),
                screenshotProperties.getExtPopup()
        );
    }
}
