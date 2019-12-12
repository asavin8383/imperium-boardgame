package restapi;

import lombok.SneakyThrows;
import org.apache.http.client.utils.URIBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Component;

@Component
public class PPPRACheckClient {

    @Value("${spring.rest_base_url}")
    private String baseUrl;

    @Autowired
    OAuth2RestTemplate restTemplate;
    @SneakyThrows
    public boolean checkRegistryIsAvailable() {
        HttpEntity<String> requestEntity = new HttpEntity<>("");
        String link = parseLink(baseUrl);
        ResponseEntity<String> response = restTemplate.exchange(parseLink(baseUrl), HttpMethod.OPTIONS, requestEntity, String.class);
        HttpStatus code = response.getStatusCode();
        if (code.is1xxInformational() | code.is2xxSuccessful() | code.is3xxRedirection())
            return true;
        return false;
    }

    @SneakyThrows
    private String parseLink(String url) {
        URIBuilder builder = new URIBuilder(url);
        String host = builder.getHost();
        return "http://" + host;
    }
}
