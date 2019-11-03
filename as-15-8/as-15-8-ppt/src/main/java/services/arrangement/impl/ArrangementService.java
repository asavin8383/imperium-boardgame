package services.arrangement.impl;

import enums.ExecutionStatus;
import events.ArrangementChannels;
import exceptions.AS_15_8_Exception;
import jobs.ArrangementJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.task.Arrangement;
import model.task.FormalTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.support.MessageBuilder;
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
@EnableBinding(ArrangementChannels.class)
public class ArrangementService {

    private final ArrangementRepo arrangementRepo;
    private final ArrangementChannels source;

    public Arrangement saveArrangement(Arrangement arrangement, FormalTask formalTask){
        arrangement.setFormalTask(formalTask);
        return arrangementRepo.save(arrangement);
    }

    /**
     * Отправка мероприятия диспетчеру для детального заполнения
     * @param arrangement мероприятие
     */
   /* public void fillArrangement(Arrangement arrangement){
        ArrangementJob arrangementJob = arrangementJobCreationService.createArrangementJob(arrangement);
        source
            .outputArrangementJobs()
            .send(
                MessageBuilder
                    .withPayload(arrangementJob)
                    .build()
                    );
        log.info("Мероприятие {} отправлено диспетчеру для заполнения", arrangement.getId());
    }*/

    public void updateArrangementPlanInfo(Arrangement arrangement){
        if(arrangement.getPlannedStartTime()==null || arrangement.getPlannedEndTime() == null){
            AS_15_8_Exception.logAndThrow(log, String.format("Ошибка изменения планового времени мероприятия. Некорректные входные параметры: дата начала - %s, дата окончания - %s", arrangement.getPlannedStartTime(), arrangement.getPlannedEndTime()));
        }
        Arrangement updateArrangement =
                arrangementRepo.findById(arrangement.getId())
                .orElseThrow(() -> new AS_15_8_Exception("Ошибка изменения планового времени мероприятия. Мероприятие с ИД: " + arrangement.getId() + " не было найдено в БД"));
        updateArrangement.setPlannedStartTime(arrangement.getPlannedStartTime());
        updateArrangement.setPlannedEndTime(arrangement.getPlannedEndTime());
        arrangementRepo.save(updateArrangement);
    }

    public Page<Arrangement> findPageByStatus(ExecutionStatus status, PageRequest page){
        return arrangementRepo.findPageByStatus(ExecutionStatus.FORMED, page);
    }

}
