package controllers;

import arrangement.ArrangementStatusNotification;
import enums.ArrangementEvents;
import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.enums.ExecutionStatus;
import model.task.Arrangement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import services.arrangement.ArrangementNotificationService;

import java.time.LocalDateTime;

/**
 * Created by san
 * Date: 02.11.2019
 */
@RestController
@Slf4j
@RequestMapping(path = "/arrangements/status", produces = MediaType.APPLICATION_JSON_VALUE)

@RequiredArgsConstructor(onConstructor_={@Autowired})
public class ArrangementNotificationController {

    private final ArrangementNotificationService arrangementNotificationService;

    @PreAuthorize("hasRole('ROLE_SYSTEM')")
    @PutMapping
    public void consumeArrangementNotification(@RequestBody ArrangementStatusNotification arrangementStatusNotification){
        log.info("Получен запрос на изменение статуса мероприятия : " + arrangementStatusNotification.toString());
        try {
            arrangementNotificationService.processNotification(arrangementStatusNotification);
        } catch (Exception ex) {
            log.error("Ошибка обработки запроса на изменение статуса мероприятия: " + arrangementStatusNotification.toString(), ex);
        }
    }

    @PreAuthorize("hasAnyRole('ROLE_MANAGE_ARRANGEMENT')")
    @PutMapping("/stopped")
    public ResponseEntity changeExecutionStatusToStopped(@RequestParam("id") Arrangement arrangement) {
        if (arrangement == null)
            throw new AS_15_8_PPT_Exception("Мероприятие не найдено");
        if (arrangement.getStatus().equals(ExecutionStatus.ACT_SENT)) {
            if (!arrangement.getContainsUncompletedCheckUnits())
                return ResponseEntity.badRequest().body(
                        "У мероприятия нет незавершённых проверок id: " + arrangement.getId());
            ArrangementStatusNotification arrangementStatusNotification = new ArrangementStatusNotification();
            arrangementStatusNotification.setArrangementId(arrangement.getId());
            arrangementStatusNotification.setEvent(ArrangementEvents.STOP);
            arrangementStatusNotification.setEventDate(LocalDateTime.now());

            arrangementNotificationService.processNotification(arrangementStatusNotification);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().body(
                    "Недопутимый статус! Операция доступна только для мероприятий со статусом ACT_SENT, текущий статус: " + arrangement.getStatus());
        }
    }

}
