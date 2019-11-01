package common;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;


@Configuration
public class RestTemplateConfiguration {

    /**
     * Для внутренних межсервисных взаимодействий
     * @return
     */
    @Bean(name = "internal")
    RestTemplate getRestTemplate() {
        return new RestTemplate();
    }


}
