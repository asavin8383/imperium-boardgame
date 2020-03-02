package restapi;

import arrangement.ArrangementStatusNotification;
import enums.ArrangementEvents;
import exceptions.AS_15_8_DispatcherException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

/**
 * Created by san
 * Date: 02.11.2019
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ArrangementRestApi {

    private final String PPM_STATUS_ENDPOINT = "/ppm/arrangements/close";
    private final String PPT_STATUS_ENDPOINT = "/ppt/arrangements/status";
    //private final String PPT_ARRANGEMENT_EXECUTION_STATUS = "/arrangements/execution_status";
    private final String PPT_IS_ACT_AVAILABLE_FOR_AUTOMATIC_SEND = "/arrangements/act_available_for_automatic_send";
    private final String PPT_ACT_SENT_STATUS = "/arrangements/act_sent_status";
    private final String PPT_ARRANGEMENT_INTERRUPT_VIOLATION_NUMBER = "/arrangements/interrupt_violation_number";
    private final OAuth2RestTemplate restTemplate;

    @Value("${gateway.url}")
    private String gatewayUrl;

    public boolean sendStatusNotificationToPPM(Long arrangementId, boolean isStopped){
        return sendStatusNotification(arrangementId, isStopped, PPM_STATUS_ENDPOINT);
    }

    public boolean sendStatusNotificationToPPT(Long arrangementId, boolean isStopped){
        return sendStatusNotification(arrangementId, isStopped, PPT_STATUS_ENDPOINT);
    }

    private boolean sendStatusNotification(Long arrangementId, boolean isStopped, String path){
        ArrangementStatusNotification notification = new ArrangementStatusNotification(
                arrangementId,
                isStopped ? ArrangementEvents.STOP : ArrangementEvents.FINISH
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        MappingJacksonValue jacksonValue = new MappingJacksonValue(notification);
        HttpEntity<MappingJacksonValue> entity = new HttpEntity<>(jacksonValue, headers);

        log.info("Отправка сообщения с изменением статуса мероприятия {}, путь: " + path, arrangementId);
        try {
            restTemplate.put(UriComponentsBuilder
                    .fromHttpUrl(gatewayUrl)
                    .path(path)
                    .queryParam("id", arrangementId)
                    .build().toString(), entity);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.info("Ошибка отправки сообщения с изменением статуса мероприятия, " + arrangementId + " путь: " + path + ", код возврата " + ex.getStatusCode());
            return false;
        }
        log.info("Сообщение с изменением статуса мероприятия {} успешно отправлено, путь: " + path, arrangementId);
        return true;
    }


    /*public ExecutionStatus getArrangementExcecutionStatus(Long arrangementId) {
        log.info("Отправка сообщения с запросом статуса мероприятия {} в ППТ", arrangementId);
        try {
            return restTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(gatewayUrl).path(PPT_ARRANGEMENT_EXECUTION_STATUS).queryParam("id", arrangementId).build().toString(), ExecutionStatus.class);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw AS_15_8_DispatcherException.logAndGet(log, String.format("Ошибка отправки сообщения с запросом статуса мероприятия %d в ППТ, код возврата %s", arrangementId, ex.getStatusCode()));
        }
    }*/

    public boolean isActAvailableFromPPT(Long arrangementId) {
        try {
            Optional<Boolean> result = restTemplate.exchange(
                    createUri(arrangementId, PPT_IS_ACT_AVAILABLE_FOR_AUTOMATIC_SEND),
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<Optional<Boolean>>(){}
            ).getBody();
            //FIXME Как то много опшеналов...
            return Optional.ofNullable(result).map(res -> res.orElse(false)).orElse(false);
        } catch (Exception ex) {
            throw AS_15_8_DispatcherException.logAndGet(log, String.format("Ошибка отправки сообщения с запросом статуса доступности отправки акта мероприятию %d в ППТ", arrangementId));
        }
    }

    public void changeArrangementStatusToActSentPPT(Long arrangementId) {
        try {
            restTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(gatewayUrl).path(PPT_ACT_SENT_STATUS).queryParam("id", arrangementId).build().toString(), Boolean.class);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw AS_15_8_DispatcherException.logAndGet(log, String.format("Ошибка отправки сообщения с изменение статуса ACT_SENT %d в ППТ, код возврата %s", arrangementId, ex.getStatusCode()));
        }
    }

    public Long getInterruptViolationNumberFromPPT(Long arrangementId) {
        try {
            ResponseEntity<Long> result = restTemplate.getForEntity(
                    createUri(arrangementId, PPT_ARRANGEMENT_INTERRUPT_VIOLATION_NUMBER),
                    Long.class
            );

            if (result.getBody() != null) {
                return result.getBody();
            } else return null;

        } catch (Exception ex) {
            log.error("Ошибка запроса предельного числа проверок для прерывания мероприятия " + arrangementId + " из ППТ", ex);
            return null;
        }
    }

    private String createUri(Long arrangementId, String path) {
        return UriComponentsBuilder
                .fromHttpUrl(gatewayUrl)
                .path(path)
                .queryParam("id", arrangementId)
                .build()
                .toString();
    }
}
