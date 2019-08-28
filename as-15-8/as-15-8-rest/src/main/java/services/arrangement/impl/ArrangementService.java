package services.arrangement.impl;

import enums.ExecutionStatus;
import exceptions.AS_15_8_Exception;
import jobs.ArrangementJob;
import events.ArrangementChannels;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.result.ArrangementResult;
import model.task.Arrangement;
import model.task.FormalTask;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import repositories.ArrangementRepo;
import repositories.ArrangementResultRepo;
import services.arrangement.ArrangementJobCreationService;

import java.util.*;

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
    private final ArrangementJobCreationService arrangementJobCreationService;
    private final ArrangementChannels source;
    private final ArrangementResultRepo arrangementResultRepo;

    public Arrangement saveArrangement(Arrangement arrangement, FormalTask formalTask){
        arrangement.setFormalTask(formalTask);
        return arrangementRepo.save(arrangement);
    }

    /**
     * Отправка мероприятия диспетчеру для детального заполнения
     * @param arrangement мероприятие
     */
    public void fillArrangement(Arrangement arrangement){
        ArrangementJob arrangementJob = arrangementJobCreationService.createArrangementJob(arrangement);
        source
            .outputArrangementJobs()
            .send(
                MessageBuilder
                    .withPayload(arrangementJob)
                    .build()
                    );
        log.info("Мероприятие {} отправлено диспетчеру для заполнения", arrangement.getId());
    }

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

    public Map<Arrangement, TreeSet<ArrangementResult>> getArrangementCheckUnits(List<Long> arrangementIds){
        Map<Arrangement, TreeSet<ArrangementResult>> arrangementCheckUnits = new HashMap<>();
        arrangementIds.forEach(arrangementId -> {
            Arrangement arrangement = arrangementRepo.findById(arrangementId)
                    .orElseThrow(() -> new AS_15_8_Exception("Ошибка создания расписания! Мероприятие не было найдено по ID: " + arrangementId));
            TreeSet<ArrangementResult> arrangementResults = new TreeSet<>(Comparator.comparingLong(ArrangementResult::getId));
            arrangementResults.addAll(arrangementResultRepo.findAllByArrangement(arrangement));
            arrangementCheckUnits.put(arrangement, arrangementResults);
        });
        return arrangementCheckUnits;
    }

}
