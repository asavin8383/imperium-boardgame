package services.arrangement.impl;

import java.util.Comparator;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import exceptions.AS_15_8_PPT_Exception;
import lombok.extern.slf4j.Slf4j;
import enums.ExecutionStatus;
import model.task.Arrangement;
import model.task.ArrangementView;
import model.task.FormalTask;
import repositories.ArrangementRepo;
import repositories.ArrangementViewRepo;
import repositories.FormalTaskRepository;
import services.arrangement.ArrangementStatusService;

/**
 * Creation date: 04.06.2019
 * Author: asavin
 */
@Service
@Slf4j
public class ArrangementStatusServiceImpl implements ArrangementStatusService {

    private ArrangementRepo arrangementRepo;
    private FormalTaskRepository formalTaskRepo;
    private ArrangementViewRepo arrangementViewRepo;

    @Autowired
    public ArrangementStatusServiceImpl(ArrangementRepo arrangementRepo,
                                        FormalTaskRepository formalTaskRepo,
                                        ArrangementViewRepo arrangementViewRepo) {
        this.arrangementRepo = arrangementRepo;
        this.formalTaskRepo = formalTaskRepo;
        this.arrangementViewRepo = arrangementViewRepo;
    }

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
            ArrangementView arrangementView = new ArrangementView();
            arrangementView.setUser(arrangement.getFormalTask().getUser());
            arrangementView.setArrangement(arrangement);
            arrangementView.setStatus(arrangement.getStatus());
            arrangementView.setViewed(false);
            arrangementViewRepo.save(arrangementView);
        }
    }
}
