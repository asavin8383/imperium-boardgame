package captcha;

import captcha.impl.RecaptchaV2Solver;

import java.util.HashMap;
import java.util.Map;

public class CaptchaSolverFactory {

    private static final Map<CaptchaType, CaptchaSolver> captchaSolvers = new HashMap<CaptchaType, CaptchaSolver>(){{
        put(CaptchaType.RECAPTCHA_V2, new RecaptchaV2Solver());
    }};

    public static CaptchaSolver createCaptchaSolver(CaptchaType captchaType) throws CaptchaSolverException {
        try {
            return captchaSolvers.get(captchaType);
        } catch (IllegalArgumentException ex){
            throw new CaptchaSolverException("Ошибка! Капча " + captchaType.name() + " не поддерживается");
        }
    }
}
