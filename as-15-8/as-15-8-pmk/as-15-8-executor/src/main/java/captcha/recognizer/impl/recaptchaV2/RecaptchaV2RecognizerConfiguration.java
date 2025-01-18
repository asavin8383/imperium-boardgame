package captcha.recognizer.impl.recaptchaV2;

import captcha.recognizer.CaptchaRecognizer;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.CommonsPool2TargetSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RecaptchaV2RecognizerConfiguration {

    @Value("${spring.cloud.stream.bindings.jobs.consumer.concurrency}")
    private Integer maxConcurrent;

    @Bean
    public CommonsPool2TargetSource recaptchaV2RecognizerPooledTargetSource() {
        final CommonsPool2TargetSource commonsPoolTargetSource = new CommonsPool2TargetSource();
        commonsPoolTargetSource.setTargetBeanName("recaptchaV2Recognizer");
        commonsPoolTargetSource.setTargetClass(RecaptchaV2Recognizer.class);
        commonsPoolTargetSource.setMaxSize(this.maxConcurrent);
        return commonsPoolTargetSource;
    }

    @Bean
    @Autowired
    public ProxyFactoryBean recaptchaV2RecognizerProxyFactoryBean(CommonsPool2TargetSource recaptchaV2RecognizerPooledTargetSource) {
        final ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setTargetSource(recaptchaV2RecognizerPooledTargetSource);
        return proxyFactoryBean;
    }

    @Bean
    @Autowired
    public CaptchaRecognizer recaptchaV2(ProxyFactoryBean recaptchaV2RecognizerProxyFactoryBean) {
        return (CaptchaRecognizer)recaptchaV2RecognizerProxyFactoryBean.getObject();
    }
}
