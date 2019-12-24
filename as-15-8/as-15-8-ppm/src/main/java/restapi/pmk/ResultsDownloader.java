package restapi.pmk;

import exceptions.AS_15_8_PPM_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ResultsDownloader {
    private final String URI = "/dispatcher/results/not_planned_not_running";

    @Value("${gateway.url}")
    private String gatewayUrl;

    private final OAuth2RestTemplate oAuth2RestTemplate;

    public Long getNotPlannedNotRunningResults(Long arrangementId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        log.debug("Отправка запроса на получение количества заверёшнных результатов в ПМК: {}", arrangementId);
        try {
            return oAuth2RestTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(gatewayUrl).path(URI).queryParam("id", arrangementId).build().toString(), Long.class);
            //log.info("Запрос на получение количества заверёшнных результатов успешно отправлен в ПМК");
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw AS_15_8_PPM_Exception.logAndGet(log, String.format("Ошибка отправки запроса на получение количества заверёшнных результатов в ПМК, код возврата %s", ex.getStatusCode()));
        }
    }
}
