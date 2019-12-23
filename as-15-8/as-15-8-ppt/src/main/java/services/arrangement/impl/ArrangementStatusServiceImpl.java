package services.arrangement.impl;

import enums.ExecutionStatus;
import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.task.Arrangement;
import model.task.ClientNotification;
import model.task.FormalTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repositories.ArrangementRepo;
import repositories.ClientNotificationRepo;
import repositories.FormalTaskRepository;
import services.arrangement.ArrangementStatusService;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.Optional;

/**
 * Creation date: 04.06.2019
 * Author: asavin
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ArrangementStatusServiceImpl implements ArrangementStatusService {

    private final ArrangementRepo arrangementRepo;
    private final FormalTaskRepository formalTaskRepo;
    private final ClientNotificationRepo clientNotificationRepo;

    @Override
    @Transactional
    public void processArrangementStatusChange(Arrangement arrangement) {
        log.info("Changing arrangement status. Arrangement id: " + arrangement.getId() + ", arrangement status: " + arrangement.getStatus());
        arrangementRepo.save(arrangement);
        //Если нужно, сохраним мероприятие в таблицу для просмотра
        saveArrangementView(arrangement);
        formalTaskRepo.findById(arrangement.getFormalTask().getId())
                .map(formalTask -> {
                    //Если статус сменился, будем сохранять
                    determineFormalTaskStatus(formalTask)
                        .ifPresent(status -> {
                            if(!formalTask.getStatus().equals(status)){
                                formalTask.setStatus(status);
                                if(status.equals(ExecutionStatus.FINISHED)){
                                    formalTask.setEndDate(LocalDateTime.now());
                                } else if (status.equals(ExecutionStatus.RUNNING)){
                                    if(formalTask.getStartDate() == null ||
                                            (arrangement.getStartDate() != null && formalTask.getStartDate().isAfter(arrangement.getStartDate()))){
                                        formalTask.setStartDate(arrangement.getStartDate());
                                    }
                                }
                                formalTaskRepo.save(formalTask);
                                log.info("Formal task status changed. Formal task id: " + formalTask.getId() + ", new status: " + formalTask.getStatus());
                            }
                        });

                    return true;
                })
                .orElseThrow(() -> new AS_15_8_PPT_Exception("Error updating arrangement status! Formal task was not found by id: " + arrangement.getFormalTask().getId()));
    }

    private Optional<ExecutionStatus> determineFormalTaskStatus(FormalTask formalTask){
        return formalTask.getArrangements().stream()
                .map(Arrangement::getStatus)
                .min(Comparator.comparing(ExecutionStatus::getPriority));
    }

    private void saveArrangementView(Arrangement arrangement){
        if (arrangement.getStatus().equals(ExecutionStatus.ACTION_REQUIRED) || arrangement.getStatus().equals(ExecutionStatus.FINISHED)){
            ClientNotification clientNotification = new ClientNotification();
            clientNotification.setOperator(arrangement.getFormalTask().getOperator());
            clientNotification.setMessageText("Статус мероприятия " + arrangement.getId() + " сменился на '" + arrangement.getStatus().getDescription() + "'");
            clientNotification.setViewed(false);
            clientNotificationRepo.save(clientNotification);

            log.info("Сохранение поручения ", clientNotification);
            log.info("clientNotification.getOperator = " + clientNotification.getOperator());
        }
    }
}
