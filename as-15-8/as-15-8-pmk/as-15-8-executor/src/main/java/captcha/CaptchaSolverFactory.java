package captcha;

import captcha.impl.RecaptchaV2Solver;

import java.util.HashMap;
import java.util.Map;

public class CaptchaSolverFactory {

    private static final Map<CaptchaType, CaptchaSolver> captchaSolvers = new HashMap<CaptchaType, CaptchaSolver>(){{
        put(CaptchaType.RECAPTCHA_V2, new RecaptchaV2Solver());
    }};

    public static CaptchaSolver createCaptchaSolver(String captchaName) throws CaptchaSolverException {
        try {
            CaptchaType captchaType = CaptchaType.valueOf(captchaName);
            return captchaSolvers.get(captchaType);
        } catch (IllegalArgumentException ex){
            throw new CaptchaSolverException("Ошибка! Капча " + captchaName + " не поддерживается");
        }
    }
}
