package captcha.solver;

import captcha.CaptchaType;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public interface CaptchaSolver {

    CaptchaType getCaptchaType();
    void solve(WebDriver driver, WebElement captchaElement) throws CaptchaSolverException;
}
