package screenshot;

import common.ApplicationConfiguration;
import common.ExecutorProperties;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.Platform;
import org.openqa.selenium.WebDriver;
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
    public void testScreenshot() {

        WebDriver driver = DriverFactory.createDriver(
                ExecutorProperties.getSeleniumHubUrl(),
                Platform.ANY,
                "chrome",
                "119.0"
        );

        driver.get("https://google.com");

        byte[] screen = ScriptUtils.getScreenshot(driver);

        File outputFile = new File("screen.jpg");
        try (FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            outputStream.write(screen);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
