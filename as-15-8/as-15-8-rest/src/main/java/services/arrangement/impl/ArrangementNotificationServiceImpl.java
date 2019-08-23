package services.arrangement.impl;

import arrangement.ArrangementStatusNotification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
public class ArrangementNotificationServiceImpl implements ArrangementNotificationService {

    private ArrangementRepo arrangementRepo;
    private ArrangementStatusService arrangementStatusService;

    @Autowired
    public ArrangementNotificationServiceImpl(ArrangementRepo arrangementRepo,
                                              ArrangementStatusService arrangementStatusService) {
        this.arrangementRepo = arrangementRepo;
        this.arrangementStatusService = arrangementStatusService;
    }

    @Override
    public void processNotification(ArrangementStatusNotification arrangementStatusNotification) {
        arrangementRepo.findById(arrangementStatusNotification.getArrangementId())
            .map(arrangement -> {
                arrangement.sendEvent(arrangementStatusNotification.getEvent());
                try {
                    arrangementStatusService.processArrangementStatusChange(arrangement);
                    log.info("Статус мероприятия {} сменился на: {}", arrangement.getId(), arrangement.getStatus());
                    return true;
                } catch (Exception ex) {
                    log.error("не удалось сменить статус мероприятия {} на {}", arrangement.getId(), arrangement.getStatus(), ex);
                    return false;
                }
            })
            .orElseGet(() -> {
                log.error("Ошибка смены статуса мероприятия. Мероприятие не было найдено по ID: {}", arrangementStatusNotification.getArrangementId());
                return false;
            });
    }
}
