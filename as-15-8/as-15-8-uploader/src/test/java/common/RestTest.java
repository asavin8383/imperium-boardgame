package common;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import restapi.PASDRestClient;
import restapi.PSRestClient;
import restapi.SubTypeRestClient;

@Configuration
@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations = "classpath:application.yml")
public class RestTest
{
    @Autowired
    PSRestClient psRestClient;

    @Autowired
    PASDRestClient pasdRestClient;

    @Autowired
    SubTypeRestClient subTypeRestClient;

    /*@Bean
    public RestTemplate restTemplateInit() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor("test158", "test158"));
        return restTemplate;
    }*/

    @Test
    public void testPS(){
        psRestClient.readFromNet();

    }

    @Test
    public void testPASD(){
        pasdRestClient.readFromNet();

    }

    @Test
    public void testSybType(){
        subTypeRestClient.readFromNet();

    }

}
