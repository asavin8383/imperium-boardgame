package restapi;

import enums.ErdiStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.protocol.types.Field;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Created by san
 * Date: 09.02.2020
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ConfigClient {
    private final String ROBOTS_ENDPOINT = "/config/robots/orig_name";
    private final OAuth2RestTemplate restTemplate;

    @Value("${gateway.url}")
    private String gatewayUrl;

    public Map<String,String> getRobotInfo(String name){
        try {
            return restTemplate.exchange(
                UriComponentsBuilder
                    .fromHttpUrl(gatewayUrl)
                    .path(ROBOTS_ENDPOINT)
                    .queryParam("name", name)
                    .build().toString(),
                HttpMethod.POST,
                null,
                new ParameterizedTypeReference<Map<String, String>>(){})
                .getBody();
        } catch (Exception ex) {
            log.error("Ошибка получения информации о роботе для штампа", ex);
            return new HashMap<>();
        }
    }
}
