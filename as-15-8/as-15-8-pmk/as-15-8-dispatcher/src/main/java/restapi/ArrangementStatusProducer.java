package restapi;

import arrangement.ArrangementStatusNotification;
import enums.ExecutionStatus;
import exceptions.AS_15_8_DispatcherException;
import lombok.NonNull;
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
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.Optional;

/**
 * Created by san
 * Date: 02.11.2019
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ArrangementStatusProducer {

    private final String PPT_STATUS_ENDPOINT = "/ppt/arrangements/status";
    private final String PPM_STATUS_ENDPOINT = "/ppm/arrangements/close";
    private final String PPT_ARRANGEMENT_EXECUTION_STATUS = "/arrangements/execution_status";
    private final String PPM_COMPLETION_ENDPOINT = "/ppm/arrangements/completion";
    private final OAuth2RestTemplate restTemplate;
    private final Integer FINISHED = 100;

    @Value("${gateway.url}")
    private String gatewayUrl;

    public void sendArrangementStatusMessage(ArrangementStatusNotification arrangementStatusNotification){
        Long arrId = arrangementStatusNotification.getArrangementId();
        Optional<Integer> completion = getCompletionFromPPM(arrId);
        if (completion.isPresent()) {
            if (completion.get().equals(FINISHED)) {
                sendToPPT(arrangementStatusNotification);
                sendToPPM(arrangementStatusNotification.getArrangementId());
            }
        }
    }

    private Optional<Integer> getCompletionFromPPM(Long arrangementId) {
        try {
            Integer completion = restTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(gatewayUrl).path(PPM_COMPLETION_ENDPOINT).queryParam("id", arrangementId).build().toString(), Integer.class);
            return Optional.ofNullable(completion);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw AS_15_8_DispatcherException.logAndGet(log, String.format("Ошибка получения процента выполнения мероприятия из ППМ, код возврата %s", arrangementId, ex.getStatusCode()));
        }
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
        HttpEntity<?> requestEntity = new HttpEntity(new HttpHeaders());
        log.info("Отправка сообщения с изменением статуса мероприятия {} в ППМ", arrangementId);
        try {
            restTemplate.put(UriComponentsBuilder.fromHttpUrl(gatewayUrl).path(PPM_STATUS_ENDPOINT).queryParam("id", arrangementId).build().toString(), requestEntity);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw AS_15_8_DispatcherException.logAndGet(log, String.format("Ошибка отправки сообщения с изменением статуса мероприятия %d в ППМ, код возврата %s", arrangementId, ex.getStatusCode()));
        }
        log.info("Сообщение с изменением статуса мероприятия {} успешно отправлено в ППМ", arrangementId);
    }


    public ExecutionStatus getArrangementExcecutionStatus(Long arrangementId) {
        log.info("Отправка сообщения с запросом статуса мероприятия {} в ППТ", arrangementId);
        try {
            return restTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(gatewayUrl).path(PPT_ARRANGEMENT_EXECUTION_STATUS).queryParam("id", arrangementId).build().toString(), ExecutionStatus.class);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw AS_15_8_DispatcherException.logAndGet(log, String.format("Ошибка отправки сообщения с запросом статуса мероприятия %d в ППТ, код возврата %s", arrangementId, ex.getStatusCode()));
        }
    }

}
