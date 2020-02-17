package restapi;

import accessTools.AccessToolDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Created by san
 * Date: 09.02.2020
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ConfigClient {
    private final String ROBOTS_ENDPOINT = "/config/access_tool_info";
    private final OAuth2RestTemplate restTemplate;

    @Value("${gateway.url}")
    private String gatewayUrl;

    public AccessToolDTO getRobotInfo(String name){
        try {
            return restTemplate.postForObject(
                UriComponentsBuilder
                    .fromHttpUrl(gatewayUrl)
                    .path(ROBOTS_ENDPOINT)
                    .queryParam("name", name)
                    .build().toString(),
                null,
                AccessToolDTO.class);
        } catch (Exception ex) {
            log.error("Ошибка получения информации о роботе для штампа", ex);
            //Возвращаем пустые объекты, чтобы не сломать скриншот
            return new AccessToolDTO(" ", " ", " ");
        }
    }
}
