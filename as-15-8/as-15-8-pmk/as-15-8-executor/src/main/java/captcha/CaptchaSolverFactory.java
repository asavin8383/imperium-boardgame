package captcha;

import captcha.impl.RecaptchaV2Solver;
import robots.exceptions.ExecutionException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CaptchaSolverFactory {

    private static final Map<CaptchaType, CaptchaSolver> captchaSolvers = new HashMap<CaptchaType, CaptchaSolver>(){{
        try {
            put(CaptchaType.RECAPTCHA_V2, new RecaptchaV2Solver());
        } catch (IOException e) {
            throw new ExecutionException(e);
        }
    }};

    public static CaptchaSolver createCaptchaSolver(CaptchaType captchaType) throws CaptchaSolverException {
        try {
            return captchaSolvers.get(captchaType);
        } catch (IllegalArgumentException ex){
            throw new CaptchaSolverException("Ошибка! Капча " + captchaType.name() + " не поддерживается");
        }
    }
}
