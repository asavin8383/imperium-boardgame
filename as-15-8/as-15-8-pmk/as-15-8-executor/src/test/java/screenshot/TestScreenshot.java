package screenshot;

import common.ApplicationConfiguration;
import common.ExecutorProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.PropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import robots.DriverFactory;
import robots.utils.ScriptUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ApplicationConfiguration.class})
@PropertySource("file:config/application.yml")
public class TestScreenshot {

    @Test
    public void testScreenshot() throws InterruptedException {

        WebDriver driver = DriverFactory.createDriver(
                ExecutorProperties.getSeleniumHubUrl(),
                Platform.ANY,
                "chrome",
                "undetected_119",
                "http://proxy.eclsoft.ru:3128",
                false
        );

        // String url = "https://bot.incolumitas.com/#botBehavior";
        // String url = "https://nowsecure.nl";
        // String url = "https://hmaker.github.io/selenium-detector/";
        String url = "https://google.ru";
        // String url = "https://bot.sannysoft.com/";
        // String url = "https://antcpt.com/score_detector/";
//        driver.get(url);

        JavascriptExecutor js = (JavascriptExecutor) driver;
        js.executeScript("window.open('" + url + "', '_blank');");
        Thread.sleep(3000);
        driver.switchTo().window(driver.getWindowHandles()
                .toArray()[driver.getWindowHandles().size() - 1].toString());

        Thread.sleep(15000);

        byte[] screen = ScriptUtils.getScreenshot(driver);

        File outputFile = new File("screen.jpg");
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(screen);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        driver.close();
    }
}
