package common;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.web.client.RestTemplate;

import java.net.InetSocketAddress;
import java.net.Proxy;

@Configuration
public class RestTemplateConfiguration {

    @Value("${registry-anonymizers.proxy-ip}")
    private String proxyIp;

    @Value("${registry-anonymizers.proxy-port}")
    private int proxyPort;

    @Value("${registry-anonymizers.username}")
    private String username;

    @Value("${registry-anonymizers.password}")
    private String password;

    /**
     * Для общения с внешними системами, проксированный
     * @return
     */
    @Primary
    @Bean
    public RestTemplate registryAnonimyzersRestTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        if (!Strings.isEmpty(proxyIp)){
            Proxy proxy = new Proxy(java.net.Proxy.Type.HTTP, new InetSocketAddress(proxyIp, proxyPort));
            requestFactory.setProxy(proxy);
        }
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(username, password));
        return restTemplate;
    }

    /**
     * Для внутренних межсервисных взаимодействий
     * @return
     */
    @Bean(name = "internal")
    RestTemplate getRestTemplate() {
        return new RestTemplate();
    }


}
