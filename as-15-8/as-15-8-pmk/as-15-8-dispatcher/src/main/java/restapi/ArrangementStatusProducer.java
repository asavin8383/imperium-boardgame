package restapi;

import arrangement.ArrangementStatusNotification;
import enums.ExecutionStatus;
import exceptions.AS_15_8_DispatcherException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Arrangement;
import model.Views;
import model.enums.ArrangementStatus;
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
import repositories.ArrangementRepo;
import services.ActService;

import java.util.Optional;

/**
 * Created by san
 * Date: 02.11.2019
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ArrangementStatusProducer {

    private final String PPM_STATUS_ENDPOINT = "/ppm/arrangements/close";
    private final String PPT_ARRANGEMENT_EXECUTION_STATUS = "/arrangements/execution_status";
    private final String PPT_IS_ACT_AVAILABLE_FOR_AUTOMATIC_SEND = "/arrangements/act_available_for_automatic_send";
    private final String PPT_ACT_SENT_STATUS = "/arrangements/act_sent_status";
    private final OAuth2RestTemplate restTemplate;
    private final ActService actService;
    private final ArrangementRepo arrangementRepo;

    @Value("${gateway.url}")
    private String gatewayUrl;

    public void sendArrangementStatusMessage(ArrangementStatusNotification arrangementStatusNotificationForPPT){
        Long arrangementId = arrangementStatusNotificationForPPT.getArrangementId();
        //TODO Переделать смену статуса с параллельного на последовательный.
        //TODO Переделано. Проверить.
        if (sendToPPM(arrangementId, arrangementStatusNotificationForPPT)) {
            finishArrangement(arrangementId);
            sendAct(arrangementId);
        }
    }

    private void finishArrangement(Long arrangementId){
        Arrangement arr = arrangementRepo.findById(arrangementId)
                .orElseThrow(() -> new AS_15_8_DispatcherException("Ошибка при закрытии мероприятия. Мероприятие не найдено по ID: " + arrangementId));
        arr.setStatus(ArrangementStatus.FINISHED);
        arrangementRepo.save(arr);
    }

    public Boolean sendToPPM(Long arrangementId, ArrangementStatusNotification arrangementStatusNotification){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        MappingJacksonValue jacksonValue = new MappingJacksonValue(arrangementStatusNotification);
        jacksonValue.setSerializationView(Views.Brief.class);
        HttpEntity<MappingJacksonValue> entity = new HttpEntity<>(jacksonValue, headers);

        log.info("Отправка сообщения с изменением статуса мероприятия {} в ППМ", arrangementId);
        try {
            restTemplate.put(UriComponentsBuilder.fromHttpUrl(gatewayUrl).path(PPM_STATUS_ENDPOINT).queryParam("id", arrangementId).build().toString(), entity);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.info("Ошибка отправки сообщения с изменением статуса мероприятия %d в ППМ, код возврата %s", arrangementId, ex.getStatusCode());
            return false;
        }
        log.info("Сообщение с изменением статуса мероприятия {} успешно отправлено в ППМ", arrangementId);
        return true;
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
            Optional<Boolean> result = restTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(gatewayUrl).path(PPT_IS_ACT_AVAILABLE_FOR_AUTOMATIC_SEND).queryParam("id", arrangementId).build().toString(), Optional.class);
            if (result.isPresent())
                return result.get();
            else return false;
        } catch (Exception ex) {
            throw AS_15_8_DispatcherException.logAndGet(log, String.format("Ошибка отправки сообщения с запросом статуса доступности отправки акта мероприятию %d в ППТ", arrangementId));
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
