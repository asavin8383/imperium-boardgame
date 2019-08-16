package controllers;

import com.fasterxml.jackson.annotation.JsonView;
import enums.ExecutionStatus;
import exceptions.AS_15_8_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Views;
import model.result.ArrangementResult;
import model.schedule.Schedule;
import model.task.Arrangement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.ArrangementRepository;
import repositories.ArrangementResultRepository;
import services.schedule.ScheduleService;

import java.util.*;

/**
 * Creation date: 16.08.2019
 * Author: asavin
 */
@RestController
@RequestMapping(path = "/schedule", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
@RequiredArgsConstructor(onConstructor_={@Autowired})
@Slf4j
public class ScheduleController {

    private final ArrangementRepository arrangementRepo;
    private final ScheduleService scheduleService;
    private final ArrangementResultRepository arrangementResultRepo;

    @GetMapping(path = "/arrangements")
    public List<Arrangement> getArrangementsForSchedule(){
        return arrangementRepo.findAllByStatus(ExecutionStatus.FORMED);
    }

    @PostMapping(path = "/new", consumes = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(Views.Full.class)
    public Schedule createNewSchedule(@RequestBody List<Long> arrangementIds){
        Map<Arrangement, TreeSet<ArrangementResult>> arrangementCheckUnits = new HashMap<>();
        arrangementIds.forEach(arrangementId -> {
            Arrangement arrangement = arrangementRepo.findById(arrangementId)
                    .orElseThrow(() -> new AS_15_8_Exception("Ошибка создания расписания! Мероприятие не было найдено по ID: " + arrangementId));
            TreeSet<ArrangementResult> arrangementResults = new TreeSet<>(Comparator.comparingLong(ArrangementResult::getId));
            arrangementResults.addAll(arrangementResultRepo.findAllByArrangement(arrangement));
            arrangementCheckUnits.put(arrangement, arrangementResults);
        });
        return scheduleService.create(arrangementCheckUnits);
    }

}
