package restapi;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.RestStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import rest.ResponseStatusString;


/**
 * Для взаимодействия с сервисом POD
 */
@Service
@Slf4j
public class PODExchange
{
    @Value("${gateway.url}")
    private String gatewayUrl;

    @Autowired
    private OAuth2RestTemplate oAuth2RestTemplate;

    public ResponseStatusString checkUrl(String url) {
        UriComponents uriComponents =
                UriComponentsBuilder
                        .fromHttpUrl(gatewayUrl)
                        .path("/pod/check_erdi")
                        .queryParam("url", url == null ? "" : url)
                        .build();

        log.info("GET checkUrl from {}", uriComponents.toString());

        ResponseStatusString resp = oAuth2RestTemplate.getForObject(uriComponents.toString(), ResponseStatusString.class);

        log.info("Got checkUrl status: {}", resp.toString());

        return resp;
    }

}
