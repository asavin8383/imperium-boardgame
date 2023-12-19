package captcha.impl;

import captcha.AudioRecognizer;
import captcha.CaptchaSolver;
import captcha.CaptchaSolverException;
import captcha.CaptchaUtils;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Random;

@Slf4j
public class RecaptchaV2Solver implements CaptchaSolver {

    @Override
    public void solve(WebDriver driver) throws CaptchaSolverException {
        try {
            WebElement recaptchaIframe = driver.findElement(
                    By.xpath("//iframe[@title=\"reCAPTCHA\"]"));
            clickToCaptcha(driver, recaptchaIframe);
        } catch (Exception ex) {
            throw new CaptchaSolverException("Ошибка при решении RecaptchaV2", ex);
        }
    }

    private void clickToCaptcha(WebDriver driver, WebElement recaptchaIframe) throws Exception {
        driver.switchTo().frame(recaptchaIframe);

        WebElement checkbox = waitForElement(driver, By.id("recaptcha-anchor"), 10);

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
    }

    private void solveChallenge(WebDriver driver, WebElement captchaChallenge) throws Exception {
        driver.switchTo().frame(captchaChallenge);

        try{
            waitForElement(driver, By.xpath("//*[@id=\"recaptcha-audio-button\"]"), 1)
                    .click();
        } catch (Exception ignored){

        }

        solveAudioChallenge(driver);

        WebElement verifyButton = waitForElement(driver, By.id("recaptcha-verify-button"), 5);
        jsClick(driver, verifyButton);
        randomDelay();

        try{
            waitForElement(driver,
                    By.xpath("//div[normalize-space()=\"Multiple correct solutions required - please solve more.\"]"),
                    1
            );

            solveAudioChallenge(driver);

            WebElement secondVerifyButton = waitForElement(driver, By.id("recaptcha-verify-button"), 5);
            jsClick(driver, secondVerifyButton);
        } catch (TimeoutException ignored) {}

        driver.switchTo().parentFrame();
    }

    private void solveAudioChallenge(WebDriver driver) throws Exception {

        WebElement downloadLink;
        try{
            downloadLink = waitForElement(driver, By.className("rc-audiochallenge-tdownload-link"), 10);
        } catch (Exception ex) {
            throw new CaptchaSolverException("Google обнаружил автоматические запросы. Решить капчу невозможно.");
        }

        Path tempDir = Files.createTempDirectory(System.getProperty("java.io.tmpdir"));
        File mp3File = Files.createTempFile(tempDir, "recaptcha_v2", ".mp3").toFile();

        String link = downloadLink.getAttribute("href");
        CaptchaUtils.downloadFile(link, mp3File);

        String recognizedText = AudioRecognizer.recognize(mp3File);

        if(mp3File.exists()){
            boolean deleted = mp3File.delete();
            if(!deleted)
                log.warn("Не удалось удалить временный файл: " + mp3File.getAbsolutePath());
        }

        WebElement responseTextbox = driver.findElement(By.id("audio-response"));
        humanType(responseTextbox, recognizedText);
    }

    private void jsClick(WebDriver driver, WebElement element){
        ((JavascriptExecutor)driver).executeScript("arguments[0].click();", element);
    }

    private WebElement waitForElement(WebDriver driver, By by, long timeout) {
        WebDriverWait wait = new WebDriverWait(driver, timeout);
        return wait.until(ExpectedConditions.visibilityOfElementLocated(by));
    }

    private void randomDelay() throws InterruptedException {
        Random random = new Random();
        Thread.sleep(random.nextInt(1250 - 750 + 1) + 750);
    }

    private void humanType(WebElement element, String text) throws InterruptedException {
        Random random = new Random();
        for(char sym : text.toCharArray()) {
            element.sendKeys(String.valueOf(sym));
            Thread.sleep(random.nextInt(100 - 50 + 1) + 50);
        }
    }
}
