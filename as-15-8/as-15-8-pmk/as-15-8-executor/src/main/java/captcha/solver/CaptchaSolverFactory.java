package captcha.solver;

import captcha.CaptchaType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CaptchaSolverFactory {

    private final List<CaptchaSolver> captchaSolvers;

    private static final Map<CaptchaType, CaptchaSolver> captchaSolversCache = new HashMap<>();

    @PostConstruct
    public void initServicesCache() {
        captchaSolvers.forEach(solver ->
                captchaSolversCache.put(solver.getCaptchaType(), solver)
        );
    }

    public static CaptchaSolver getSolver(CaptchaType captchaType) throws CaptchaSolverException {
        try {
            return captchaSolversCache.get(captchaType);
        } catch (IllegalArgumentException ex){
            throw new CaptchaSolverException("Ошибка! Капча " + captchaType.name() + " не поддерживается");
        }
    }
}
