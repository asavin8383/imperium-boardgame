package captcha.solver.impl;

import captcha.*;
import captcha.recognizer.AudioRecognizer;
import captcha.solver.CaptchaSolver;
import captcha.solver.CaptchaSolverException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.Random;

@Slf4j
@Component
public class RecaptchaV2Solver implements CaptchaSolver {

    @Autowired
    private AudioRecognizer recognizer;

    @Override
    public CaptchaType getCaptchaType() {
        return CaptchaType.RECAPTCHA_V2;
    }

    @Override
    public void solve(WebDriver driver, WebElement recaptchaIframe) throws CaptchaSolverException {
        try {
            driver.switchTo().frame(recaptchaIframe);

            WebElement checkbox = waitForElement(driver, By.id("recaptcha-anchor"), 20);

            jsClick(driver, checkbox);

            if(checkbox.getAttribute("aria-checked").equals("true")){
                return;
            }

            randomDelay();

            driver.switchTo().parentFrame();

            WebElement captchaChallenge = waitForElement(driver,
                    By.xpath("//iframe[contains(@src, \"recaptcha\") and contains(@src, \"bframe\")]"),
                    5
            );

            solveChallenge(driver, captchaChallenge);
        } catch (Exception ex) {
            throw new CaptchaSolverException("Ошибка при решении RecaptchaV2", ex);
        }
    }

    private void solveChallenge(WebDriver driver, WebElement captchaChallenge) throws Exception {
        driver.switchTo().frame(captchaChallenge);

        try{
            waitForElement(driver, By.xpath("//*[@id=\"recaptcha-audio-button\"]"), 10)
                    .click();
        } catch (Exception ignored){

        }

        solveAndTypeAudio(driver);

        for(int i = 0; i < 3; i++){
            if(!checkAndSolveRepeatedCaptcha(driver)){
                break;
            }
        }

        driver.switchTo().parentFrame();
    }

    private boolean checkAndSolveRepeatedCaptcha(WebDriver driver) throws Exception {
        try{
            waitForElement(driver,
                    By.xpath("//div[normalize-space()=\"Multiple correct solutions required - please solve more.\"]"),
                    2
            );

            solveAndTypeAudio(driver);
            return true;
        } catch (TimeoutException ignored) {
            return false;
        }
    }

    private void solveAndTypeAudio(WebDriver driver) throws Exception {
        solveAudioChallenge(driver);

        WebElement secondVerifyButton = waitForElement(driver, By.id("recaptcha-verify-button"), 15);
        jsClick(driver, secondVerifyButton);

        randomDelay();
    }

    private void solveAudioChallenge(WebDriver driver) throws Exception {

        WebElement downloadLink;
        try{
            downloadLink = waitForElement(driver, By.className("rc-audiochallenge-tdownload-link"), 20);
        } catch (Exception ex) {
            throw new CaptchaSolverException("Google обнаружил автоматические запросы. Решить капчу невозможно.");
        }

        File mp3File = Files.createTempFile("recaptcha_v2", ".mp3").toFile();

        String link = downloadLink.getAttribute("href");

        Proxy proxy = (Proxy) ((RemoteWebDriver)driver).getCapabilities().getCapability("proxy");
        URL proxyUrl = proxy == null ? null : new URL("http://" + proxy.getHttpProxy());
        CaptchaUtils.downloadFile(link, mp3File, proxyUrl);

        String recognizedText = recognizer.recognize(mp3File);

        if(mp3File.exists()){
            boolean deleted = mp3File.delete();
            if(!deleted)
                log.warn("Не удалось удалить временный файл: " + mp3File.getAbsolutePath());
        }

        WebElement responseTextbox = waitForElement(driver, By.id("audio-response"), 15);
        humanType(responseTextbox, recognizedText);
    }

    private void jsClick(WebDriver driver, WebElement element){
        ((JavascriptExecutor)driver).executeScript("arguments[0].click();", element);
    }

    private WebElement waitForElement(WebDriver driver, By by, long timeout) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    private void randomDelay() {
        Random random = new Random();
        try {
            Thread.sleep(random.nextInt(1250 - 750 + 1) + 750);
        } catch (InterruptedException ex) {
            log.warn("Ошибка ожидания", ex);
        }
    }

    private void humanType(WebElement element, String text) {
        Random random = new Random();
        element.click();
        for(char sym : text.toCharArray()) {
            element.sendKeys(new String(Character.toChars(sym)));
            try {
                Thread.sleep(random.nextInt(100 - 50 + 1) + 50);
            } catch (InterruptedException ex) {
                log.warn("Ошибка ожидания ввода текста", ex);
            }
        }
    }
}
