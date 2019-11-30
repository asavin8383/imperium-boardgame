package restapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

/**
 * Created by san
 * Date: 02.11.2019
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ErdiChecker {

    private final String CHECK_ENDPOINT = "/pod/erdi/expired";

    private final OAuth2RestTemplate restTemplate;

    @Value("${gateway.url}")
    private String gatewayUrl;

    public boolean isExpired(Long erdiId){
        try {
            return Optional.ofNullable(restTemplate.getForObject(
                    UriComponentsBuilder
                            .fromHttpUrl(gatewayUrl)
                            .path(CHECK_ENDPOINT)
                            .queryParam("id", erdiId)
                            .build().toString(),
                    Boolean.class)).orElse(false);
        } catch (Exception ex) {
            log.error("Ошибка при проверке времени создания ЕРДИ", ex);
            return false;
        }
    }
}
