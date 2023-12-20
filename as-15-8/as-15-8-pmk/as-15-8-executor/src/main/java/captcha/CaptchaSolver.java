package captcha;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public interface CaptchaSolver {
    public void solve(WebDriver driver, WebElement captchaElement) throws CaptchaSolverException;
}
