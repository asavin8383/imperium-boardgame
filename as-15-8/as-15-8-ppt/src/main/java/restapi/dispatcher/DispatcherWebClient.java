package restapi.dispatcher;

import arrangement.ArrangementStatusNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DispatcherWebClient {

    @Value("${gateway.url}")
    private String gatewayUrl;

    private final String DISPATCHER_SET_STOPPING_REASOM_TO_NORMAL = "/dispatcher/arrangements/stoping_reason_normal";
    private final OAuth2RestTemplate restTemplate;

    public long getCompletion(Long arrangementId) {

        return 0;
    }

    public boolean setStoppingReasonToNormal(Long arrangementId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        MappingJacksonValue jacksonValue = new MappingJacksonValue(arrangementId);
        HttpEntity<MappingJacksonValue> entity = new HttpEntity<>(jacksonValue, headers);

        log.info("Отправка сообщения с изменением статуса мероприятия {} в ПМК, путь: {} ",
                arrangementId,
                DISPATCHER_SET_STOPPING_REASOM_TO_NORMAL);
        try {
            restTemplate.put(UriComponentsBuilder
                    .fromHttpUrl(gatewayUrl)
                    .path(DISPATCHER_SET_STOPPING_REASOM_TO_NORMAL)
                    .build().toString(), entity);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.info("Ошибка отправки сообщения с изменением статуса мероприятия в PMK, путь: {}, ошибка: {} ", DISPATCHER_SET_STOPPING_REASOM_TO_NORMAL, ex);
            return false;
        }
        log.info("Сообщение с изменением статуса мероприятия {} успешно отправлено, путь: " + DISPATCHER_SET_STOPPING_REASOM_TO_NORMAL, arrangementId);
        return true;
    }
}
