package controllers;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import controllers.helpers.SortingHelper;
import enums.ExecutionStatus;
import enums.SortingDirection;
import exceptions.AS_15_8_Exception;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Views;
import model.enums.ScheduleStatus;
import model.result.ArrangementResult;
import model.schedule.Schedule;
import model.schedule.SchedulePeriod;
import model.task.Arrangement;
import model.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import services.arrangement.impl.ArrangementService;
import services.schedule.ScheduleService;
import services.user.UserService;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

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

    private final ArrangementService arrangementService;
    private final ScheduleService scheduleService;
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
        return arrangementService.findPageByStatus(ExecutionStatus.FORMED, page);
    }

    @GetMapping
    @JsonView(Views.Full.class)
    public Schedule getSchedule(@RequestParam("id") Schedule schedule){
        return schedule;
    }


    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(Views.Full.class)
    @ResponseStatus(code = HttpStatus.CREATED)
    public Schedule postSchedule(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate plannedDate,
            @RequestBody List<Long> arrangementIds,
            Authentication auth){
        Schedule schedule = createSchedule(arrangementIds, ((User)auth.getPrincipal()).getUserName(), plannedDate);
        return scheduleService.saveSchedule(schedule);
    }

    @PutMapping
    @JsonView(Views.Full.class)
    @Transactional
    public Schedule updateSchedule(
            @RequestParam("id") Schedule schedule,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate plannedDate,
            @RequestBody List<Arrangement> arrangements,
            Authentication auth){
        if(!(schedule.getStatus().equals(ScheduleStatus.NEW))){
            AS_15_8_Exception.logAndThrow(log, String.format("Ошибка изменения расписания! Некорректный статус расписания с ИД: %d - %s", schedule.getId(), schedule.getStatus()));
        }
        //Если изменились плановые значения времени, сначала нужно изменить само мероприятие
        arrangements.stream()
                .filter(arrangement -> arrangement.getPlannedStartTime()!=null && arrangement.getPlannedEndTime()!=null)
                .forEach(arrangementService::updateArrangementPlanInfo);
        List<Long> arrangementIds = arrangements.stream().map(Arrangement::getId).collect(Collectors.toList());
        Schedule newSchedule = createSchedule(arrangementIds, ((User)auth.getPrincipal()).getUserName(), plannedDate);
        newSchedule.setId(schedule.getId());
        return scheduleService.saveSchedule(newSchedule);
    }

    @PutMapping(path = "/plan")
    @JsonView(Views.Id.class)
    public Schedule planSchedule(
            @RequestParam("id") Schedule schedule
    ) {
        if(!(schedule.getStatus().equals(ScheduleStatus.NEW))){
            AS_15_8_Exception.logAndThrow(log, String.format("Ошибка планирования расписания! Некорректный статус расписания с ИД: %d - %s", schedule.getId(), schedule.getStatus()));
        }
        return scheduleService.planSchedule(schedule);
    }

    @DeleteMapping
    public void deleteSchedule(@RequestParam("id") Schedule schedule){
        scheduleService.deleteSchedule(schedule);
    }

    @GetMapping(path = "/gant")
    @JsonView(Views.Full.class)
    public List<BriefArrangement> getScheduleGant(@RequestParam("id") Schedule schedule){
        List<BriefArrangement> briefArrangements = new ArrayList<>();
        SortedSet<SchedulePeriod> schedulePeriods = schedule.getSchedulePeriods();
        schedulePeriods.forEach(schedulePeriod -> schedulePeriod.getSchedulePeriodArrangements()
                .forEach(schedulePeriodArrangement -> {
                    BriefArrangement briefArrangement = new BriefArrangement(schedulePeriodArrangement.getArrangementId(), schedulePeriod.getStartTime(), schedulePeriod.getEndTime());
                    if(briefArrangements.contains(briefArrangement)){
                        briefArrangements.get(briefArrangements.indexOf(briefArrangement)).setPlannedEndTime(briefArrangement.getPlannedEndTime());
                    } else {
                        briefArrangements.add(briefArrangement);
                    }
                }));
        return briefArrangements;
    }

    private Schedule createSchedule(List<Long> arrangementIds, String userName, LocalDate plannedDate){
        User user = userService.getUserByUserName(userName);
        if(plannedDate==null || plannedDate.isBefore(LocalDate.now())){
            plannedDate = LocalDate.now();
        }
        log.info("Начало расчета расписания на дату: {}", plannedDate);
        Map<Arrangement, TreeSet<ArrangementResult>> arrangementCheckUnits = arrangementService.getArrangementCheckUnits(arrangementIds);
        for(Map.Entry<Arrangement, TreeSet<ArrangementResult>> entry: arrangementCheckUnits.entrySet()){
            if(entry.getValue().isEmpty()){
                AS_15_8_Exception.logAndThrow(log, "Ошибка создания расписания. У мероприятия " + entry.getKey().getId() + " пустое множество значений для проверки");
            }
        }
        Schedule schedule = scheduleService.create(arrangementCheckUnits);
        log.info("Расчет расписания на дату {} завершен", plannedDate);
        schedule.setUser(user);
        schedule.setPlannedDate(plannedDate);
        return schedule;
    }

    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @AllArgsConstructor
    class BriefArrangement {
        @EqualsAndHashCode.Include
        @JsonView(Views.Id.class)
        private Long id;
        @JsonView(Views.Full.class)
        private LocalTime plannedStartTime;
        @JsonView(Views.Full.class)
        private LocalTime plannedEndTime;
    }

}
