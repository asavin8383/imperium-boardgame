package controllers;

import arrangement.ArrangementStatusNotification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import services.arrangement.ArrangementNotificationService;

/**
 * Created by san
 * Date: 02.11.2019
 */
@RestController
@Slf4j
@RequestMapping(path = "/arrangements/status", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_SYSTEM')")
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class ArrangementNotificationController {

    private final ArrangementNotificationService arrangementNotificationService;

    @PutMapping
    public void consumeArrangementNotification(@RequestBody ArrangementStatusNotification arrangementStatusNotification){
        log.info("Получен запрос на изменение статуса мероприятия : " + arrangementStatusNotification.toString());
        try {
            arrangementNotificationService.processNotification(arrangementStatusNotification);
        } catch (Exception ex) {
            log.error("Ошибка обработки запроса на изменение статуса мероприятия: " + arrangementStatusNotification.toString(), ex);
        }
    }


}
