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


/**
 * Для взаимодействия с сервисом POD
 */
@Service
@Slf4j
public class PODExchange
{
    @Value("${gateway.url}")
    private String gatewayUrl;

    @Autowired @Qualifier("internal")
    private OAuth2RestTemplate oAuth2RestTemplate;


    public boolean checkUrl(String url) {
        String base = gatewayUrl.endsWith("/") ? gatewayUrl : gatewayUrl + "/";
        UriComponents uriComponents =
                UriComponentsBuilder.fromUriString("{base}/pod/check_erdi")
                        .queryParam("url", url == null ? "" : url)
                        .build()
                        .expand(base);

        log.info("GET checkUrl from {}", uriComponents.toString());

        RestStatus resp = oAuth2RestTemplate.getForObject(uriComponents.toString(), RestStatus.class);
        boolean res = resp != null && resp.status;

        log.info("Got checkUrl status: {}", res);

        return res;
    }

}
