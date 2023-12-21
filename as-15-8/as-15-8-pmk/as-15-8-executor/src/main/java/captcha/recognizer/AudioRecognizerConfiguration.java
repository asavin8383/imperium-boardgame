package captcha.recognizer;

import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aop.target.CommonsPool2TargetSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AudioRecognizerConfiguration {

    @Value("$spring.cloud.stream.bindings.jobs.consumer.concurrency")
    private int maxConcurrent;

    @Bean
    public CommonsPool2TargetSource AudioRecognizerPooledTargetSource() {
        final CommonsPool2TargetSource commonsPoolTargetSource = new CommonsPool2TargetSource();
        commonsPoolTargetSource.setTargetBeanName("audioRecognizer");
        commonsPoolTargetSource.setTargetClass(AudioRecognizer.class);
        commonsPoolTargetSource.setMaxSize(this.maxConcurrent);
        return commonsPoolTargetSource;
    }

    @Bean
    @Autowired
    public ProxyFactoryBean AudioRecognizerProxyFactoryBean(CommonsPool2TargetSource audioRecognizerPooledTargetSource) {
        final ProxyFactoryBean proxyFactoryBean = new ProxyFactoryBean();
        proxyFactoryBean.setTargetSource(audioRecognizerPooledTargetSource);
        return proxyFactoryBean;
    }

    @Bean
    @Autowired
    public AudioRecognizer recognizer(ProxyFactoryBean audioRecognizerProxyFactoryBean) {
        return (AudioRecognizer)audioRecognizerProxyFactoryBean.getObject();
    }
}
