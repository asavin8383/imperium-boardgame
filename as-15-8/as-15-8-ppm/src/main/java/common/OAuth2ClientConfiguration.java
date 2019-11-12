package common;


import com.netflix.discovery.AbstractDiscoveryClientOptionalArgs;
import com.netflix.discovery.DiscoveryClient;
import io.pivotal.spring.cloud.service.eureka.ClientFilterAdapter;
import io.pivotal.spring.cloud.service.eureka.EurekaOAuth2RequestDecorator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;

import java.util.ArrayList;
import java.util.List;


@Configuration
@EnableOAuth2Client
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class OAuth2ClientConfiguration {

    private final EurekaOAuth2RequestDecorator eurekaOauth2RequestDecorator;

    /**
     * Для внутренних межсервисных взаимодействий
     * @return OAuth2RestTemplate
     */
    @Bean
    public OAuth2RestTemplate oauth2RestTemplate(OAuth2ClientContext oauth2ClientContext,
                                                 OAuth2ProtectedResourceDetails details) {
        return new OAuth2RestTemplate(details, oauth2ClientContext);
    }

    /**
     * Бин для подключения к Eureka через Oauth.
     * Переопределение по аналогии с io.pivotal.spring.cloud.service.eureka.EurekaOAuth2AutoConfiguration discoveryClientOptionalArgs
     * Бин из автоконфигурации не создается, тк библиотека config server уже определяет такой бин
     * @return Бин из автоконфигурации
     */
    @Bean
    @SuppressWarnings("unsafe")
    public DiscoveryClient.DiscoveryClientOptionalArgs discoveryClientOptionalArgs() {
        List<? super ClientFilterAdapter> filters = new ArrayList();
        filters.add(new ClientFilterAdapter(this.eurekaOauth2RequestDecorator));
        AbstractDiscoveryClientOptionalArgs args = new DiscoveryClient.DiscoveryClientOptionalArgs();
        args.setAdditionalFilters(filters);
        return (DiscoveryClient.DiscoveryClientOptionalArgs)args;
    }

}
