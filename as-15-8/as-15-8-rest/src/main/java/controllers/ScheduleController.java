package controllers;

import com.fasterxml.jackson.annotation.JsonView;
import controllers.helpers.SortingHelper;
import enums.ExecutionStatus;
import enums.SortingDirection;
import exceptions.AS_15_8_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Views;
import model.enums.ScheduleStatus;
import model.result.ArrangementResult;
import model.schedule.Schedule;
import model.task.Arrangement;
import model.user.User;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
    public Page<Arrangement> getArrangementsForSchedule(
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize
    ){
        PageRequest page = PageRequest.of(
                pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
        return arrangementRepo.findPageByStatus(ExecutionStatus.FORMED, page);
    }


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(Views.Full.class)
    @ResponseStatus(code = HttpStatus.CREATED)
    public Schedule postSchedule(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> plannedDate,
            @RequestBody List<Long> arrangementIds,
            Authentication auth){
        Schedule schedule = createSchedule(arrangementIds, ((User)auth.getPrincipal()).getUserName(), plannedDate);
        return scheduleService.saveSchedule(schedule);
    }

    @PutMapping
    @JsonView(Views.Full.class)
    public ResponseEntity<Schedule> updateSchedule(
            @RequestParam("id") Schedule schedule,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Optional<LocalDate> plannedDate,
            @RequestBody List<Long> arrangementIds,
    Authentication auth){
        if(!(schedule.getStatus().equals(ScheduleStatus.NEW))){
            AS_15_8_Exception.logAndThrow(log, String.format("Некорректный статус расписания с ИД: %d - %s", schedule.getId(), schedule.getStatus()));
        }
        Schedule newSchedule = createSchedule(arrangementIds, ((User)auth.getPrincipal()).getUserName(), plannedDate);
        newSchedule.setId(schedule.getId());
        return new ResponseEntity<>(scheduleService.updateSchedule(schedule, newSchedule), HttpStatus.OK) ;
    }



    @DeleteMapping
    public void deleteSchedule(@RequestParam("id") Schedule schedule){
        scheduleService.deleteSchedule(schedule);
    }

    private Schedule createSchedule(List<Long> arrangementIds, String userName, Optional<LocalDate> plannedDate){
        User user = userService.getUserByUserName(userName);
        LocalDate scheduleDate = plannedDate.orElse(LocalDate.now());
        log.info("Начало расчета расписания на дату: {}", scheduleDate);
        Map<Arrangement, TreeSet<ArrangementResult>> arrangementCheckUnits = new HashMap<>();
        arrangementIds.forEach(arrangementId -> {
            Arrangement arrangement = arrangementRepo.findById(arrangementId)
                    .orElseThrow(() -> new AS_15_8_Exception("Ошибка создания расписания! Мероприятие не было найдено по ID: " + arrangementId));
            TreeSet<ArrangementResult> arrangementResults = new TreeSet<>(Comparator.comparingLong(ArrangementResult::getId));
            arrangementResults.addAll(arrangementResultRepo.findAllByArrangement(arrangement));
            arrangementCheckUnits.put(arrangement, arrangementResults);
        });
        Schedule schedule = scheduleService.create(arrangementCheckUnits);
        log.info("Расчет расписания на дату {} завершен", scheduleDate);
        schedule.setUser(user);
        schedule.setPlannedDate(scheduleDate);
        return schedule;
    }

}
