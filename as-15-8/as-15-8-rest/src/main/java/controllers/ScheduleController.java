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
import model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import repositories.ArrangementRepository;
import repositories.ArrangementResultRepository;
import services.schedule.ScheduleService;
import services.user.UserService;

import java.time.LocalDate;
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
    private final UserService userService;

    @GetMapping(path = "/arrangements")
    public List<Arrangement> getArrangementsForSchedule(){
        return arrangementRepo.findAllByStatus(ExecutionStatus.FORMED);
    }

    @PostMapping(path = "/new", consumes = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(Views.Full.class)
    public Schedule createNewSchedule(@RequestBody List<Long> arrangementIds){
        return createSchedule(arrangementIds);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(Views.Id.class)
    @ResponseStatus(code = HttpStatus.CREATED)
    public Schedule saveSchedule(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> plannedDate,
            @RequestBody List<Long> arrangementIds,
            Authentication auth){
        User user = userService.getUserByUserName(((User)auth.getPrincipal()).getUserName());
        log.info("Начало расчета расписания");
        Schedule schedule = createSchedule(arrangementIds);
        log.info("Расчет расписания завершен");
        schedule.setUser(user);
        schedule.setPlannedDate(plannedDate.orElse(LocalDate.now()));
        return scheduleService.saveSchedule(schedule);
    }

    @DeleteMapping
    public void deleteSchedule(@RequestParam("id") Schedule schedule){
        scheduleService.deleteSchedule(schedule);
    }

    private Schedule createSchedule(List<Long> arrangementIds){
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
