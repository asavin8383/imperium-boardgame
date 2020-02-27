package utils;

import common.AnalyzerProperties;
import enums.AccessToolParameter;
import enums.AccessToolUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Created by san
 * Date: 27.02.2020
 */
@Slf4j
public class ScreenshotAnalyzerHelper {

    @Autowired
    private AnalyzerProperties analyzerProperties;

    public boolean screenshotRequired(String accessTool){
        AccessToolUnit accessToolUnit = analyzerProperties.getAccessToolUnit(accessTool)
            .orElseThrow(() -> new RuntimeException("Ошибка при получении типа робота "+accessTool));
        Map<AccessToolParameter, String> props = analyzerProperties.getProps().getAccessToolUnits().get(accessToolUnit)
            .getRobotProps().get(accessTool).getProps();
        return !props.containsKey(AccessToolParameter.MAKE_SCREENSHOT_ON_COMPLETED)
            || Boolean.parseBoolean(props.get(AccessToolParameter.MAKE_SCREENSHOT_ON_COMPLETED));
    }
}
