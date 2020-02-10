package restapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by san
 * Date: 09.02.2020
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PptClient {
    private final String ARRANGEMENTS_ENDPOINT = "/arrangements/access_tool";
    private final OAuth2RestTemplate restTemplate;

    @Value("${gateway.url}")
    private String gatewayUrl;

    public String getAccessTool(Long arrangementId){
        try {
            return restTemplate.getForObject(
                UriComponentsBuilder
                    .fromHttpUrl(gatewayUrl)
                    .path(ARRANGEMENTS_ENDPOINT)
                    .queryParam("id", arrangementId)
                    .build().toString(),
               String.class);
        } catch (Exception ex) {
            log.error("Ошибка получения информации о названии ПС/ПАСД для штампа", ex);
            return "";
        }
    }
}
