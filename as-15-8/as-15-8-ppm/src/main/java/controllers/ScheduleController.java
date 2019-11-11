package controllers;

import com.fasterxml.jackson.annotation.JsonView;
import enums.SortingDirection;
import exceptions.AS_15_8_PPM_Exception;
import helpers.SortingHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import repositories.ScheduleRepo;
import services.ArrangementService;
import services.ScheduleService;

import java.security.Principal;
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
@RequiredArgsConstructor(onConstructor_={@Autowired})
@Slf4j
public class ScheduleController {

    private final ArrangementService arrangementService;
    private final ScheduleService scheduleService;
    private final ScheduleRepo scheduleRepo;

    @GetMapping(path = "/arrangements")
    public Page<Arrangement> getArrangementsForSchedule(
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize
    ){
        PageRequest page = PageRequest.of(
                pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
        return arrangementService.findPage(page);
    }

    @GetMapping("/all")
    @JsonView(Views.Full.class)
    public List<Schedule> getScheduleList(@RequestParam("date") LocalDate plannedDate){
        return scheduleRepo.findAllByPlannedDate(plannedDate);
    }

    @GetMapping
    @JsonView(Views.Full.class)
    public Schedule getSchedule(@RequestParam("id") Schedule schedule){
        return schedule;
    }

    @GetMapping(path = "/total_workers_count")
    public Integer getTotalWorkersCount(){
        return scheduleService.getTotalWorkersCount();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(Views.Full.class)
    @ResponseStatus(code = HttpStatus.CREATED)
    public Schedule postSchedule(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate plannedDate,
            @RequestBody List<Long> arrangementIds,
            Principal principal){
        Schedule schedule = createSchedule(filterAvailableArrangements(arrangementIds), principal.getName(), plannedDate);
        return scheduleService.saveSchedule(schedule);
    }

    @PutMapping
    @JsonView(Views.Full.class)
    @Transactional
    public Schedule updateSchedule(
            @RequestParam("id") Schedule schedule,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate plannedDate,
            @RequestBody List<Arrangement> arrangements,
            Principal principal){
        if(!(schedule.getStatus().equals(ScheduleStatus.NEW))){
            throw AS_15_8_PPM_Exception.logAndGet(log, String.format("Ошибка изменения расписания! Некорректный статус расписания с ИД: %d - %s", schedule.getId(), schedule.getStatus()));
        }
        //Сначала исключим из расписания все периоды
        scheduleService.clearSchedulePeriods(schedule);
        //Если изменились плановые значения времени, сначала нужно изменить само мероприятие
        arrangements.stream()
                .filter(arrangement -> arrangement.getPlannedStartTime()!=null && arrangement.getPlannedEndTime()!=null)
                .forEach(arrangementService::updateArrangementPlanInfo);
        List<Long> arrangementIds = arrangements.stream().map(Arrangement::getId).collect(Collectors.toList());
        Schedule newSchedule = createSchedule(filterAvailableArrangements(arrangementIds), principal.getName(), plannedDate);
        schedule.setSchedulePeriods(newSchedule.getSchedulePeriods());
        schedule.getSchedulePeriods().forEach(schedulePeriod -> schedulePeriod.setSchedule(schedule));
        schedule.setAuthor(principal.getName());
        if (plannedDate != null) {
            schedule.setPlannedDate(plannedDate);
        }
        return scheduleService.saveSchedule(schedule);
    }

    @PutMapping(path = "/plan")
    @JsonView(Views.Id.class)
    public Schedule planSchedule(
            @RequestParam("id") Schedule schedule
    ) {
        if(!(schedule.getStatus().equals(ScheduleStatus.NEW))){
            throw AS_15_8_PPM_Exception.logAndGet(log, String.format("Ошибка планирования расписания! Некорректный статус расписания с ИД: %d - %s", schedule.getId(), schedule.getStatus()));
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
                    BriefArrangement briefArrangement = new BriefArrangement(schedulePeriodArrangement.getArrangement().getId(), schedulePeriodArrangement.getArrangement().getTitle(), schedulePeriod.getStartTime(), schedulePeriod.getEndTime());
                    if(briefArrangements.contains(briefArrangement)){
                        briefArrangements.get(briefArrangements.indexOf(briefArrangement)).setPlannedEndTime(briefArrangement.getPlannedEndTime());
                    } else {
                        briefArrangements.add(briefArrangement);
                    }
                }));
        return briefArrangements;
    }

    private List<Long> filterAvailableArrangements(List<Long> arrangementIds){
        List<Long> availableIds =
                arrangementService.findAllAvailableArrangements()
                        .stream()
                        .mapToLong(Arrangement::getId)
                        .boxed()
                        .collect(Collectors.toList());
        arrangementIds.removeIf(id -> !availableIds.contains(id));
        return arrangementIds;
    }

    private Schedule createSchedule(List<Long> arrangementIds, String author, LocalDate plannedDate){
        if(plannedDate==null || plannedDate.isBefore(LocalDate.now())){
            plannedDate = LocalDate.now();
        }
        log.info("Начало расчета расписания на дату: {}", plannedDate);
        Map<Arrangement, TreeSet<ScheduleCheckUnit>> arrangementCheckUnits = arrangementService.getArrangementCheckUnits(arrangementIds);
        for(Map.Entry<Arrangement, TreeSet<ScheduleCheckUnit>> entry: arrangementCheckUnits.entrySet()){
            if(entry.getValue().isEmpty()){
                throw AS_15_8_PPM_Exception.logAndGet(log, "Ошибка создания расписания. У мероприятия " + entry.getKey().getId() + " пустое множество значений для проверки");
            }
        }
        Schedule schedule = scheduleService.create(arrangementCheckUnits);
        log.info("Расчет расписания на дату {} завершен", plannedDate);
        schedule.setAuthor(author);
        schedule.setPlannedDate(plannedDate);
        return schedule;
    }

    @Data
    @EqualsAndHashCode(onlyExplicitlyIncluded = true)
    @AllArgsConstructor
    private static class BriefArrangement {
        @EqualsAndHashCode.Include
        @JsonView(Views.Id.class)
        private Long id;
        @JsonView(Views.Full.class)
        private String title;
        @JsonView(Views.Full.class)
        private LocalTime plannedStartTime;
        @JsonView(Views.Full.class)
        private LocalTime plannedEndTime;
    }

}
