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

/**
 * Creation date: 29.05.2019
 * Author: asavin
 */
@Service
@Slf4j
public class ArrangementNotificationServiceImpl implements ArrangementNotificationService {

    private ArrangementRepository arrangementRepo;

    @Autowired
    public ArrangementNotificationServiceImpl(ArrangementRepository arrangementRepo) {
        this.arrangementRepo = arrangementRepo;
    }

    @Override
    public void processNotification(ArrangementStatusNotification arrangementStatusNotification) {
        arrangementRepo.findById(arrangementStatusNotification.getArrangementId())
            .map(arrangement -> {
                if (arrangementStatusNotification.getArrangementStatus().equals(ArrangementStatus.ACTION_REQUIRED)){
                    arrangement.setStatus(ExecutionStatus.ACTION_REQUIRED);
                } else if (arrangementStatusNotification.getArrangementStatus().equals(ArrangementStatus.FINISHED)){
                    arrangement.setStatus(ExecutionStatus.FINISHED);
                } else {
                    throw new AS_15_8_Exception("Error changing arrangement status. Status not supported: " + arrangementStatusNotification.getArrangementStatus());
                }
                arrangementRepo.save(arrangement);
                return true;

            })
            .orElseGet(() -> {
                log.error("Error changing arrangement status. Arrangement was not found by id: " + arrangementStatusNotification.getArrangementId());
                return false;
            });
    }
}
