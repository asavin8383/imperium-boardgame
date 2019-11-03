package restapi;

import exceptions.AS_15_8_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Views;
import model.task.Arrangement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Created by san
 * Date: 29.10.2019
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ArrangementUploader {

    @Value("${gateway.url}")
    private String gatewayUrl;

    @Qualifier("internal")
    private final RestTemplate restTemplate;

    public void updateArrangement(Arrangement arrangement){
        String path = "/ppm/arrangements";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        MappingJacksonValue jacksonValue = new MappingJacksonValue(arrangement);
        jacksonValue.setSerializationView(Views.Brief.class);
        HttpEntity<MappingJacksonValue> entity = new HttpEntity<>(jacksonValue, headers);

        log.info("Отправка мероприятия в ППМ: {}", arrangement.getId());
        try {
            restTemplate.put(UriComponentsBuilder.fromHttpUrl(gatewayUrl).path(path).queryParam("id", arrangement.getId()).build().toString(), entity);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
             AS_15_8_Exception.logAndThrow(log, String.format("Ошибка отправки мероприятия %d в ППМ, код возврата %s", arrangement.getId(), ex.getStatusCode()));
        }
        log.info("Мероприятие {} успешно отправлено в ППМ", arrangement.getId());
    }
}
