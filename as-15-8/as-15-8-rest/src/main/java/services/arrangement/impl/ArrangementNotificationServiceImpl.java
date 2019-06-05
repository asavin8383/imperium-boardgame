package services.arrangement.impl;

import arrangement.ArrangementStatusNotification;
import enums.ArrangementStatus;
import exceptions.AS_15_8_Exception;
import lombok.extern.slf4j.Slf4j;
import model.enums.ExecutionStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.ArrangementRepository;
import services.arrangement.ArrangementNotificationService;
import services.arrangement.ArrangementStatusService;

import java.time.LocalDateTime;

/**
 * Creation date: 29.05.2019
 * Author: asavin
 * Обработчик оповещения об изменении состояния выполнения мероприятие (приостановлен, закончен)
 */
@Service
@Slf4j
public class ArrangementNotificationServiceImpl implements ArrangementNotificationService {

    private ArrangementRepository arrangementRepo;
    private ArrangementStatusService arrangementStatusService;

    @Autowired
    public ArrangementNotificationServiceImpl(ArrangementRepository arrangementRepo,
            ArrangementStatusService arrangementStatusService) {
        this.arrangementRepo = arrangementRepo;
        this.arrangementStatusService = arrangementStatusService;
    }

    @Override
    public void processNotification(ArrangementStatusNotification arrangementStatusNotification) {
        arrangementRepo.findById(arrangementStatusNotification.getArrangementId())
            .map(arrangement -> {
                if (arrangement.getStatus().equals(ExecutionStatus.RUNNING)){
                    log.info("Arrangement status need to be changed to: " + arrangementStatusNotification.getArrangementStatus() + " for arrangement: " +arrangement.getId());
                    if (arrangementStatusNotification.getArrangementStatus().equals(ArrangementStatus.ACTION_REQUIRED)){
                        arrangement.setStatus(ExecutionStatus.ACTION_REQUIRED);
                    } else if (arrangementStatusNotification.getArrangementStatus().equals(ArrangementStatus.FINISHED)){
                        arrangement.setStatus(ExecutionStatus.FINISHED);
                        arrangement.setEndDate(LocalDateTime.now());
                    } else {
                        throw new AS_15_8_Exception("Error changing arrangement status. Status not supported: " + arrangementStatusNotification.getArrangementStatus());
                    }
                    //Меняем статус мероприятия и задания в случае необходимости
                    arrangementStatusService.processArrangementStatusChange(arrangement);
                }
                return true;
            })
            .orElseGet(() -> {
                log.error("Error changing arrangement status. Arrangement was not found by id: " + arrangementStatusNotification.getArrangementId());
                return false;
            });
    }
}
