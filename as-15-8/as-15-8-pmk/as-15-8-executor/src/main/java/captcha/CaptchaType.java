package captcha;

import org.assertj.core.util.Strings;

public enum CaptchaType {
    RECAPTCHA_V2,
    UNKNOWN;

    public static CaptchaType of(String value){
        if(Strings.isNullOrEmpty(value))
            return UNKNOWN;

        for(CaptchaType captchaType : CaptchaType.values()){
            if(captchaType.name().equals(value.toUpperCase()))
                return captchaType;
        }
        return CaptchaType.UNKNOWN;
    }
}
