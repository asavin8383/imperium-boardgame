package services.arrangement.impl;

import exceptions.AS_15_8_Exception;
import lombok.extern.slf4j.Slf4j;
import model.enums.ExecutionStatus;
import model.task.Arrangement;
import model.task.FormalTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import repositories.ArrangementRepository;
import repositories.FormalTaskRepository;
import services.arrangement.ArrangementStatusService;

import java.util.Comparator;
import java.util.Optional;

/**
 * Creation date: 04.06.2019
 * Author: asavin
 */
@Service
@Slf4j
public class ArrangementStatusServiceImpl implements ArrangementStatusService {

    private ArrangementRepository arrangementRepo;
    private FormalTaskRepository formalTaskRepo;

    @Autowired
    public ArrangementStatusServiceImpl(ArrangementRepository arrangementRepo, FormalTaskRepository formalTaskRepo) {
        this.arrangementRepo = arrangementRepo;
        this.formalTaskRepo = formalTaskRepo;
    }

    @Override
    @Transactional
    public void processArrangementStatusChange(Arrangement arrangement) {
        log.info("Changing arrangement status. Arrangement id: " + arrangement.getId() + ", arrangement status: " + arrangement.getStatus());
        arrangementRepo.save(arrangement);
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
                .orElseThrow(() -> new AS_15_8_Exception("Error updating arrangement status! Formal task was not found by id: " + arrangement.getFormalTask().getId()));
    }

    private Optional<ExecutionStatus> determineFormalTaskStatus(FormalTask formalTask){
        return formalTask.getArrangements().stream()
                .map(Arrangement::getStatus)
                .min(Comparator.comparing(ExecutionStatus::getPriority));
    }
}
