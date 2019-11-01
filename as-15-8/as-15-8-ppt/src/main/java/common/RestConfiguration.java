package common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Created by san
 * Date: 29.10.2019
 */
@Configuration
public class RestConfiguration {

    /**
     * Для внутренних межсервисных взаимодействий
     * @return
     */
    @Bean(name = "internal")
    RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
