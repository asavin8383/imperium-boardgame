package restapi;

import arrangement.ArrangementStatusNotification;
import enums.ExecutionStatus;
import exceptions.AS_15_8_DispatcherException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Arrangement;
import model.enums.ArrangementStatus;
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
import repositories.ArrangementRepo;
import services.ActService;

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
    private final String PPT_IS_ACT_AVAILABLE = "/arrangements/act_available";
    private final String PPT_ACT_SENT_STATUS = "/arrangements/act_sent_status";
    private final OAuth2RestTemplate restTemplate;
    private final ActService actService;
    private final ArrangementRepo arrangementRepo;

    @Value("${gateway.url}")
    private String gatewayUrl;

    public void sendArrangementStatusMessage(ArrangementStatusNotification arrangementStatusNotification){
        Long arrangementId = arrangementStatusNotification.getArrangementId();
        //TODO Переделать смену статуса с параллельного на последовательный
        sendToPPT(arrangementStatusNotification);
        sendToPPM(arrangementId);
        finishArrangement(arrangementId);
        sendAct(arrangementId);
    }

    private void finishArrangement(Long arrangementId){
        Arrangement arr = arrangementRepo.findById(arrangementId)
                .orElseThrow(() -> new AS_15_8_DispatcherException("Ошибка при закрытии мероприятия. Мероприятие не найдено по ID: " + arrangementId));
        arr.setStatus(ArrangementStatus.FINISHED);
        arrangementRepo.save(arr);
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

    private void sendAct(Long arrangementId) {
        if (isActAvailableFromPPT(arrangementId)) {
            actService.createAct(arrangementId);
            changeArrangemetStatusToActSentPPT(arrangementId);
        }
    }

    private boolean isActAvailableFromPPT(Long arrangementId) {
        try {
            return restTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(gatewayUrl).path(PPT_IS_ACT_AVAILABLE).queryParam("id", arrangementId).build().toString(), Boolean.class);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw AS_15_8_DispatcherException.logAndGet(log, String.format("Ошибка отправки сообщения с запросом статуса доступности отправки акта мероприятию %d в ППТ, код возврата %s", arrangementId, ex.getStatusCode()));
        }
    }

    public void changeArrangemetStatusToActSentPPT(Long arrangementId) {
        try {
            restTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(gatewayUrl).path(PPT_ACT_SENT_STATUS).queryParam("id", arrangementId).build().toString(), Boolean.class);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw AS_15_8_DispatcherException.logAndGet(log, String.format("Ошибка отправки сообщения с изменение статуса ACT_SENT %d в ППТ, код возврата %s", arrangementId, ex.getStatusCode()));
        }
    }
}
