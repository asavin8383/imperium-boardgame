package controllers;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import repositories.ScheduleRepo;
import services.ArrangementService;
import services.ScheduleService;

import javax.annotation.PostConstruct;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.stream.Collectors;

/**
 * Creation date: 16.08.2019
 * Author: asavin
 */
@RestController
@PreAuthorize("hasRole('ROLE_FORMATION_OF_SHEDULE')")
@RequestMapping(path = "/schedule", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor_={@Autowired})
@Slf4j
public class ScheduleController {

    private final ArrangementService arrangementService;
    private final ScheduleService scheduleService;
    private final ScheduleRepo scheduleRepo;
    private ObjectMapper mapper;

    @PostConstruct
    private void init(){
        mapper = new ObjectMapper();
        mapper.findAndRegisterModules();
    }

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

    //TODO Разобраться с Pageable и JsonView
    @GetMapping("/all")
    @JsonView(Views.Brief.class)
    public List<Schedule> getScheduleList(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate plannedDate/*,
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize*/){
//        PageRequest page = PageRequest.of(
//                pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
        return scheduleRepo.findAllByPlannedDate(plannedDate == null ? LocalDate.now() : plannedDate);
    }

    @GetMapping
    @JsonView(Views.Full.class)
    public Schedule getSchedule(@RequestParam("id") Schedule schedule){
        return schedule;
    }

    @GetMapping(path = "/total_workers_count")
    public Integer getTotalWorkersCount(@RequestParam("id") Schedule schedule){
        if(schedule==null){
            throw new AS_15_8_PPM_Exception("Ошибка получения количества обработчиков! Расписание ещё не создано");
        }
        return scheduleService.getFreeWorkersCount(
                LocalDate.now(),
                scheduleRepo.getScheduleStartTime(schedule.getId()),
                scheduleRepo.getScheduleEndTime(schedule.getId()));
    }

    //TODO кидать 400
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(Views.Full.class)
    @ResponseStatus(HttpStatus.CREATED)
    public Schedule postSchedule(@RequestBody ObjectNode scheduleData, Principal principal){
        List<Long> arrangementIds = this.mapper.convertValue(
                scheduleData.get("arrangementIds"),
                new TypeReference<List<Long>>() {});
        LocalDate plannedDate = null;
        if(scheduleData.has("plannedDate"))
            plannedDate = LocalDate.parse(scheduleData.get("plannedDate").asText(), DateTimeFormatter.ISO_DATE);

        return scheduleService.saveSchedule(arrangementIds, principal.getName(), plannedDate);
    }

    @PutMapping
    @JsonView(Views.Full.class)
    @Transactional
    public Schedule updateSchedule(@RequestParam("id") Schedule schedule, @RequestBody ObjectNode scheduleData, Principal principal){
        if(!(schedule.getStatus().equals(ScheduleStatus.NEW))){
            throw AS_15_8_PPM_Exception.logAndGet(log, String.format("Ошибка изменения расписания! Некорректный статус расписания с ИД: %d - %s", schedule.getId(), schedule.getStatus()));
        }

        List<Arrangement> arrangements = this.mapper.convertValue(
                scheduleData.get("arrangements"),
                new TypeReference<List<Arrangement>>() {});
        schedule.setMaxWorkersCount(scheduleData.get("maxWorkersCount").asInt());
        LocalDate plannedDate = null;
        if(scheduleData.has("plannedDate"))
            plannedDate = LocalDate.parse(scheduleData.get("plannedDate").asText(), DateTimeFormatter.ISO_DATE);

        //Если изменились плановые значения времени, сначала нужно изменить само мероприятие
        arrangements.stream()
                .filter(arrangement -> arrangement.getPlannedStartTime()!=null && arrangement.getPlannedEndTime()!=null)
                .forEach(arrangementService::updateArrangementPlanInfo);
        List<Long> arrangementIds = arrangements.stream().map(Arrangement::getId).collect(Collectors.toList());

        return scheduleService.saveSchedule(arrangementIds, principal.getName(), plannedDate, schedule);
    }

    @PutMapping(path = "/plan")
    @JsonView(Views.Id.class)
    public ResponseEntity planSchedule(@RequestParam("id") Schedule schedule) {
        try {
            if (!(schedule.getStatus().equals(ScheduleStatus.NEW)))
                throw new AS_15_8_PPM_Exception(String.format("Ошибка планирования расписания! Некорректный статус расписания с ИД: %d - %s", schedule.getId(), schedule.getStatus()));
            return ResponseEntity.ok(scheduleService.planSchedule(schedule));
        } catch (AS_15_8_PPM_Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
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
