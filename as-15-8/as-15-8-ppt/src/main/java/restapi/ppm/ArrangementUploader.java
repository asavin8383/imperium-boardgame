package restapi.ppm;

import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Views;
import model.task.Arrangement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Created by san
 * Date: 29.10.2019
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ArrangementUploader {

    private final String ARRANGEMENTS_URI = "/ppm/arrangements";

    @Value("${gateway.url}")
    private String gatewayUrl;

    private final OAuth2RestTemplate oAuth2RestTemplate;

    public void updateArrangement(Arrangement arrangement){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        MappingJacksonValue jacksonValue = new MappingJacksonValue(arrangement);
        jacksonValue.setSerializationView(Views.Brief.class);
        HttpEntity<MappingJacksonValue> entity = new HttpEntity<>(jacksonValue, headers);

        log.info("Отправка мероприятия в ППМ: {}", arrangement.getId());
        try {
            oAuth2RestTemplate.put(UriComponentsBuilder.fromHttpUrl(gatewayUrl).path(ARRANGEMENTS_URI).queryParam("id", arrangement.getId()).build().toString(), entity);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
             throw AS_15_8_PPT_Exception.logAndGet(log, String.format("Ошибка отправки мероприятия %d в ППМ, код возврата %s", arrangement.getId(), ex.getStatusCode()));
        }
        log.info("Мероприятие {} успешно отправлено в ППМ", arrangement.getId());
    }
}
