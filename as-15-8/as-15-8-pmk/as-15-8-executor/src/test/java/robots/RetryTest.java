package robots;

import checkUnits.CheckUnit;
import checkUnits.CheckUnitJob;
import checkUnits.CheckUnitType;
import common.ApplicationConfiguration;
import enums.AccessToolParameter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.Platform;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import robots.impl.CommonDirectSearchRobot;
import service.impl.RobotsServiceImpl;

import java.util.HashMap;
import java.util.Map;

import static enums.AccessToolParameter.*;
import static enums.AccessToolParameter.SEARCH_SYSTEM_XPATH_ITEM_LINK;

/**
 * Created by san
 * Date: 28.11.2023
 */
@EnableRetry
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {ApplicationConfiguration.class})
@PropertySource("file:config/application.yml")
public class RetryTest {

    @Autowired
    RobotsServiceImpl robotsService;

    public static Map<AccessToolParameter, String> googleParams() {
        Map<AccessToolParameter, String> params = new HashMap<>();
        params.put(PLATFORM, Platform.ANY.toString());
        params.put(BROWSER, "chrome");
        params.put(VERSION, "119.0");
        params.put(SEARCH_SYSTEM_URL, "https://www.google.ru");
        params.put(SEARCH_SYSTEM_RESULT_PAGE_TYPE, "pagination");
        params.put(SEARCH_SYSTEM_XPATH_INPUT_FIELD, "//input[@name=\"q\"]");
        params.put(SEARCH_SYSTEM_XPATH_CAPTCHA, "//form[@id=\"captcha-form\"]");
        params.put(SEARCH_SYSTEM_XPATH_NEXT_PAGE, "//*[@id=\"pnnext\"]");
        params.put(SEARCH_SYSTEM_XPATH_ITEM_LINK, "//div[@class=\"g\"]//div[@class=\"r\"]/a[1]");
        return params;
    }

    @Test
    public void execute() {
        CheckUnit checkUnit = new CheckUnit();
        checkUnit.setType(CheckUnitType.URL);
        checkUnit.setValue("sfjkghdsjfghskfghklsdfjghkljfhgkslg");
        CheckUnitJob job = new CheckUnitJob();
        job.setCheckUnit(checkUnit);
        job.setAccessTool("google-local-anonimyzing");
        robotsService.run(0L, job);
//
//        Map<AccessToolParameter, String> params = googleParams();
//        CommonDirectSearchRobot robot = new CommonDirectSearchRobot(params);
//
//        robot.run(checkUnit);
    }
}
