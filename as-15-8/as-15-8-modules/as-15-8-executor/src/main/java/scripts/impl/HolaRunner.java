package scripts.impl;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;

/**
 * User: asinjavin
 * Date: 11.06.2019
 * Time: 15:29
 *
 * https://www.youtube.com/watch?time_continue=489&v=ulUEKEeu41E
 *
 */
public class HolaRunner
{
    public static void main(String[] args) throws Exception {

        System.setProperty("webdriver.chrome.driver", "C:\\Users\\asinjavin\\ws\\as-15-8\\drivers\\chromedriver.exe");

        String profile_dir = "C:\\Users\\asinjavin\\ws\\as-15-8\\data";
        String hola_path = "C:\\Users\\asinjavin\\ws\\as-15-8\\drivers\\Hola-Free-VPN-Proxy-Unblocker-Chrome-Web-Mağazası_v1.139.226.crx";

        String ip_ru = getIp(profile_dir, null);
        System.out.println("ip_ru = " + ip_ru);

        HolaOptions holaOptions = new HolaOptions(new File(profile_dir));
        holaOptions.addRule("myip.com", "us");
        holaOptions.close();

        String ip_us = getIp(profile_dir, hola_path);
        System.out.println("ip_us = " + ip_us);

    }

    private static String getIp(String profile_dir, String hola_path) throws IOException {

        ChromeOptions options = new ChromeOptions();
        options.addArguments("user-data-dir="+profile_dir);
        if (hola_path != null)
            options.addExtensions(new File(hola_path));

        WebDriver webDriver = new ChromeDriver(options);
        webDriver.get("http://myip.com/");

        WebDriverWait wait = new WebDriverWait(webDriver, 10);

        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("ip")));
        String text = el.getText();

        webDriver.quit();
        return text;
    }
}


