package events.producers.rest;

import arrangement.ArrangementStatusNotification;
import exceptions.AS_15_8_PPM_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Created by san
 * Date: 03.11.2019
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ArrangementStatusUploader {
    @Value("${gateway.url}")
    private String gatewayUrl;

    private final OAuth2RestTemplate oAuth2RestTemplate;

    public void changeArrangementStatus(ArrangementStatusNotification arrangementStatusNotification){
        String path = "/ppt/arrangements/status";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ArrangementStatusNotification> entity = new HttpEntity<>(arrangementStatusNotification, headers);

        log.info("Отправка запроса на изменения статуса мероприятия в ППТ: {}", arrangementStatusNotification.toString());
        try {
            oAuth2RestTemplate.put(UriComponentsBuilder.fromHttpUrl(gatewayUrl).path(path).build().toString(), entity);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw AS_15_8_PPM_Exception.logAndGet(log, String.format("Ошибка отправки запроса на изменение статуса мероприятия в ППТ, код возврата %s", ex.getStatusCode()));
        }
        log.info("Запрос на изменение статуса мероприятия успешно отправлен в ППТ");
    }
}
