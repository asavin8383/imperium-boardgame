package services.arrangement.impl;

import enums.ExecutionStatus;
import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.task.Arrangement;
import model.task.FormalTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import repositories.ArrangementRepo;

/**
 * Creation date: 05.08.2019
 * Сервис обработки данных мероприятий
 * Author: asavin
 */

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class ArrangementService {

    private final ArrangementRepo arrangementRepo;

    public Arrangement saveArrangement(Arrangement arrangement, FormalTask formalTask){
        arrangement.setFormalTask(formalTask);
        if (formalTask.getMissionId() != null)
            arrangement.setIsActAvailable(true);
        return arrangementRepo.save(arrangement);
    }

    public Arrangement getById(Long id){
        return arrangementRepo.findById(id)
            .orElseThrow(() -> AS_15_8_PPT_Exception.logAndGet(log, String.format("Мероприятие с ИД: {} не было найдено в БД ППТ", id)));
    }

    public void updateArrangementPlanInfo(Arrangement arrangement){
        if(arrangement.getPlannedStartTime()==null || arrangement.getPlannedEndTime() == null){
            throw AS_15_8_PPT_Exception.logAndGet(log, String.format("Ошибка изменения планового времени мероприятия. Некорректные входные параметры: дата начала - %s, дата окончания - %s", arrangement.getPlannedStartTime(), arrangement.getPlannedEndTime()));
        }
        Arrangement updateArrangement =
                arrangementRepo.findById(arrangement.getId())
                .orElseThrow(() -> new AS_15_8_PPT_Exception("Ошибка изменения планового времени мероприятия. Мероприятие с ИД: " + arrangement.getId() + " не было найдено в БД"));
        updateArrangement.setPlannedStartTime(arrangement.getPlannedStartTime());
        updateArrangement.setPlannedEndTime(arrangement.getPlannedEndTime());
        arrangementRepo.save(updateArrangement);
    }

    public Page<Arrangement> findPageByStatus(ExecutionStatus status, PageRequest page){
        return arrangementRepo.findPageByStatus(ExecutionStatus.FORMED, page);
    }

}
