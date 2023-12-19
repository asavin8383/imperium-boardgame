package captcha;

import captcha.impl.RecaptchaV2Solver;

import java.util.HashMap;
import java.util.Map;

public class CaptchaSolverFactory {

    private static final Map<Captcha, CaptchaSolver> captchaSolvers = new HashMap<Captcha, CaptchaSolver>(){{
        put(Captcha.RECAPTCHA_V2, new RecaptchaV2Solver());
    }};

    public static CaptchaSolver createCaptchaSolver(String captchaName) throws CaptchaSolverException {
        try {
            Captcha captcha = Captcha.valueOf(captchaName);
            return captchaSolvers.get(captcha);
        } catch (IllegalArgumentException ex){
            throw new CaptchaSolverException("Ошибка! Капча " + captchaName + " не поддерживается");
        }
    }
}
