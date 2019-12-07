package restapi;

import arrangement.ArrangementStatusNotification;
import exceptions.AS_15_8_DispatcherException;
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
 * Date: 02.11.2019
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ArrangementStatusProducer {

    private final String PPT_STATUS_ENDPOINT = "/ppt/arrangements/status";
    private final String PPM_STATUS_ENDPOINT = "/ppm/arrangements/{id}/close";

    private final OAuth2RestTemplate restTemplate;

    @Value("${gateway.url}")
    private String gatewayUrl;

    public void sendArrangementStatusMessage(ArrangementStatusNotification arrangementStatusNotification){
        sendToPPT(arrangementStatusNotification);
        sendToPPM(arrangementStatusNotification.getArrangementId());

    }

    private void sendToPPT(ArrangementStatusNotification arrangementStatusNotification){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<ArrangementStatusNotification> entity = new HttpEntity<>(arrangementStatusNotification, headers);

        log.info("Отправка сообщения с изменением статуса мероприятия {} в ППТ", arrangementStatusNotification.getArrangementId());
        try {
            restTemplate.put(UriComponentsBuilder.fromHttpUrl(gatewayUrl).path(PPT_STATUS_ENDPOINT).build().toString(), entity);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw AS_15_8_DispatcherException.logAndGet(log, String.format("Ошибка отправки сообщения с изменением статуса мероприятия %d в ППТ, код возврата %s", arrangementStatusNotification.getArrangementId(), ex.getStatusCode()));
        }
        log.info("Сообщение с изменением статуса мероприятия {} успешно отправлено в ППТ", arrangementStatusNotification.getArrangementId());
    }

    private void sendToPPM(Long arrangementId){
        log.info("Отправка сообщения с изменением статуса мероприятия {} в ППМ", arrangementId);
        try {
            restTemplate.put(UriComponentsBuilder.fromHttpUrl(gatewayUrl).path(PPT_STATUS_ENDPOINT).buildAndExpand(arrangementId).toString(), null);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw AS_15_8_DispatcherException.logAndGet(log, String.format("Ошибка отправки сообщения с изменением статуса мероприятия %d в ППМ, код возврата %s", arrangementId, ex.getStatusCode()));
        }
        log.info("Сообщение с изменением статуса мероприятия {} успешно отправлено в ППТ", arrangementId);
    }




}
