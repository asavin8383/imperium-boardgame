package services.arrangement.impl;

import arrangement.ArrangementStatusNotification;
import enums.ArrangementEvents;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.task.Arrangement;
import org.apache.logging.log4j.util.Strings;
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
import services.arrangement.ArrangementNotificationService;
import services.arrangement.ArrangementStatusService;

/**
 * Creation date: 29.05.2019
 * Author: asavin
 * Обработчик оповещения об изменении состояния выполнения мероприятие (приостановлен, закончен)
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ArrangementNotificationServiceImpl implements ArrangementNotificationService {

    @Value("${gateway.url}")
    private String gatewayUrl;

    private final ArrangementRepo arrangementRepo;
    private final ArrangementStatusService arrangementStatusService;
    private final String PPM_STOP_ENDPOINT = "/ppm/arrangements/stop";
    private final String PPM_FINISH_ENDPOINT = "/ppm/arrangements/finish";
    private final OAuth2RestTemplate restTemplate;

    @Override
    public void processNotification(ArrangementStatusNotification arrangementStatusNotification) {
        arrangementRepo.findById(arrangementStatusNotification.getArrangementId())
            .map(arrangement -> {
                if (Strings.isNotEmpty(arrangementStatusNotification.getInfo())){
                    arrangement.setInfo(arrangementStatusNotification.getInfo());
                    arrangementRepo.save(arrangement);
                }

                if (!notifyPPMAboutStopEvent(arrangementStatusNotification)) {
                    return false;
                }
                if (!notifyPPMAboutFinishEvent(arrangementStatusNotification)) {
                    return false;
                }
                return processNotificationInPPT(arrangement, arrangementStatusNotification);

            })
            .orElseGet(() -> {
                log.error("Ошибка смены статуса мероприятия. Мероприятие не было найдено по ID: {}", arrangementStatusNotification.getArrangementId());
                return false;
            });
    }

    private boolean notifyPPMAboutStopEvent(ArrangementStatusNotification notification) {
        if (notification.getEvent().equals(ArrangementEvents.STOP ) ||
                notification.getEvent().equals(ArrangementEvents.STOP_BY_SERVICE_MODE)) {

            log.info("Отправка события {} в ППМ, arrangementId = {}",
                    notification.getEvent(),
                    notification.getArrangementId());

            return createPutRequest(notification, PPM_STOP_ENDPOINT);
        } else {
            log.info("Ошибка отправки события STOP в ППМ, arrangementId = {}, событие: {}",
                    notification.getArrangementId(),
                    notification.getEvent());
            return true;
        }
    }

    private boolean notifyPPMAboutFinishEvent(ArrangementStatusNotification notification) {
        if (notification.getEvent().equals(ArrangementEvents.FINISH)) {
            log.info("Отправка события FINISH в ППМ, arrangementId = {}",
                    notification.getArrangementId());

            return createPutRequest(notification, PPM_FINISH_ENDPOINT);
        } else {
            log.info("Ошибка отправки события FINISH в ППМ, arrangementId = {}, событие: {}",
                    notification.getArrangementId(),
                    notification.getEvent());
            return true;
        }
    }

    private boolean createPutRequest(ArrangementStatusNotification notification, String path) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        MappingJacksonValue jacksonValue = new MappingJacksonValue(notification);
        HttpEntity<MappingJacksonValue> entity = new HttpEntity<>(jacksonValue, headers);

        log.info("Отправка сообщения с изменением статуса мероприятия {} в ППМ, путь: {}, событие {} ",
                notification.getArrangementId(),
                path,
                notification.getEvent());
        try {
            restTemplate.put(UriComponentsBuilder
                    .fromHttpUrl(gatewayUrl)
                    .path(path)
                    //.queryParam("id", notification.getArrangementId())
                    .build().toString(), entity);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.info("Ошибка отправки сообщения с изменением статуса мероприятия в ППМ, путь: {}, ошибка: {} ", path, ex);
            return false;
        }
        log.info("Сообщение с изменением статуса мероприятия {} успешно отправлено, путь: " + path, notification.getArrangementId());
        return true;
    }

    private boolean processNotificationInPPT(Arrangement arrangement, ArrangementStatusNotification arrangementStatusNotification) {
        arrangement.sendEvent(arrangementStatusNotification.getEvent(), arrangementStatusNotification.getEventDate());
        try {
            arrangementStatusService.processArrangementStatusChange(arrangement);
            log.info("Статус мероприятия {} сменился в ППТ на: {} ", arrangement.getId(), arrangement.getStatus());
            return true;
        } catch (Exception ex) {
            log.error("не удалось сменить статус мероприятия {} в ППТ на {} ", arrangement.getId(), arrangement.getStatus(), ex);
            return false;
        }
    }
}
