package captcha;

import org.openqa.selenium.WebDriver;

public interface CaptchaSolver {
    public void solve(WebDriver driver) throws CaptchaSolverException;
}
