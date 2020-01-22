package controllers;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import common.SchedulerProperties;
import enums.SortingDirection;
import exceptions.AS_15_8_PPM_Exception;
import helpers.SortingHelper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.*;
import model.enums.ScheduleStatus;
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
import repositories.ArrangementRepo;
import repositories.ScheduleRepo;
import robots.SlaPeriod;
import robots.SlaType;
import services.ArrangementService;
import services.ScheduleService;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * Creation date: 16.08.2019
 * Author: asavin
 */
@RestController
@PreAuthorize("hasRole('ROLE_FORMATION_OF_SCHEDULE')")
@RequestMapping(path = "/schedule", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor_={@Autowired})
@Slf4j
public class ScheduleController {

    private final ArrangementService arrangementService;
    private final ScheduleService scheduleService;
    private final ScheduleRepo scheduleRepo;
    private ObjectMapper mapper;
    private final SchedulerProperties schedulerProperties;
    private final ArrangementRepo arrangementRepo;

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
        int freeWorkersCount = scheduleService.getFreeWorkersCount(
                LocalDate.now(),
                scheduleRepo.getScheduleStartTime(schedule.getId()),
                scheduleRepo.getScheduleEndTime(schedule.getId()));
        if(freeWorkersCount < schedule.getMaxWorkersCount())
            throw new AS_15_8_PPM_Exception("Ошибка! Свободное количество обработчиков меньше, чем заданное для расчета ("+freeWorkersCount+" < "+schedule.getMaxWorkersCount()+")");
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
    public ResponseEntity planSchedule(@RequestParam("id") Schedule schedule, @RequestBody ObjectNode scheduleData) {
        try {
            if (!(schedule.getStatus().equals(ScheduleStatus.NEW)))
                throw new AS_15_8_PPM_Exception(String.format("Ошибка планирования расписания! Некорректный статус расписания с ИД: %d - %s", schedule.getId(), schedule.getStatus()));
            if(scheduleData.has("plannedDate")) {
                LocalDate plannedDate = LocalDate.parse(scheduleData.get("plannedDate").asText(), DateTimeFormatter.ISO_DATE);
                schedule.setPlannedDate(plannedDate);
                analyzeRobotTrafficLimits(schedule);
            }
            if(!scheduleData.has("maxWorkersCount")) {
                throw new AS_15_8_PPM_Exception("Ошибка планирования расписания. Не задано количество обработчиков");
            } else {
                int maxWorkersCount = scheduleData.get("maxWorkersCount").asInt();
                if(maxWorkersCount != schedule.getMaxWorkersCount())
                    throw new AS_15_8_PPM_Exception("Ошибка планирования расписания. Количество обработчиков было изменено. Пожалуйста, пересчитайте расписание.");
            }
            return ResponseEntity.ok(scheduleService.planSchedule(schedule));
        } catch (AS_15_8_PPM_Exception ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @DeleteMapping
    public ResponseEntity deleteSchedule(@RequestParam("id") Schedule schedule){
        if(schedule == null)
            return ResponseEntity.badRequest().body("Ошибка при удалении расписания. Расписание не найдено по ID");
        scheduleService.deleteSchedule(schedule);
        return ResponseEntity.ok().build();
    }

    @GetMapping(path = "/gant")
    @JsonView(Views.Full.class)
    public ResponseEntity getScheduleGant(@RequestParam("id") Schedule schedule){
        if(schedule == null)
            return ResponseEntity.badRequest().body("Ошибка при расчете времени расписания. Расписание не найдено по ID");
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
        return ResponseEntity.ok(briefArrangements);
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

    private void analyzeRobotTrafficLimits(Schedule schedule) {
        try {
            AtomicReference<String> description = new AtomicReference<>("");
            Map<@NotNull String, List<Arrangement>> accessToolArrangementsMap = getAccessToolArrangementsMap(schedule);
            accessToolArrangementsMap.entrySet().stream().forEach(entry -> {
                @NotNull String accessTool = entry.getKey();
                log.info("Анализ расхода трафика роботом " + accessTool);
                List<Arrangement> arrangements = entry.getValue();

                if (isRealTrafficGreaterThanSlaConfig(arrangements, schedule, SlaPeriod.DAY))
                    description.set("Превышение трафика за день для " + accessTool + ";  ");
                if (isRealTrafficGreaterThanSlaConfig(arrangements, schedule, SlaPeriod.MONTH))
                    description.set("Превышение трафика за месяц для " + accessTool + ";  ");
            });
            writeDescriptionToDb(schedule, description.get());
        } catch (Exception e) {
            log.warn("Ошибка расчёта превышения трафика согласно SLA " + e);
        }
    }

    private Map<@NotNull String, List<Arrangement>> getAccessToolArrangementsMap(Schedule schedule) {
        List<Arrangement> arrangementsOfSchedule = arrangementRepo.findAllBySchedule(schedule.getId());
        return arrangementsOfSchedule.stream()
                .collect(Collectors.groupingBy(Arrangement::getAccessTool));
    }

    private boolean isRealTrafficGreaterThanSlaConfig (List<Arrangement> arrangements, Schedule schedule, SlaPeriod slaPeriod) {
        String accessTool = getAccessTool(arrangements);
        Long realTraffic = calculateRealTraffic(arrangements, schedule, slaPeriod);
        Optional<Long> trafficInSlaPeriod = getTrafficInPeriodFromSla(accessTool, slaPeriod);
        if (!trafficInSlaPeriod.isPresent()) {
            return false;
        }

        if (realTraffic > trafficInSlaPeriod.get())
            return true;
        else return false;
    }

    private Optional<Long> getTrafficInPeriodFromSla(String accessTool, SlaPeriod slaPeriod) {
        Optional<Long> trafficInPeriod = Optional.empty();
        switch (slaPeriod) {
            case DAY:
                trafficInPeriod = schedulerProperties.getRobotSlaCheckUnitsPerDay(SlaType.TRAFFIC, accessTool);
                break;
            case MONTH:
                trafficInPeriod = schedulerProperties.getRobotSlaCheckUnitsPerMonth(SlaType.TRAFFIC, accessTool);
                break;
        }
        return trafficInPeriod;
    }

    private Long calculateRealTraffic(List<Arrangement> arrangements, Schedule schedule, SlaPeriod slaPeriod) {
        String accessTool = getAccessTool(arrangements);

        Optional<Long> trafficPerCheckUnit = schedulerProperties.getRobotTrafficPerCheckUnit(accessTool);
        Long checkUnits = getCheckUnitsInPeriod(arrangements, schedule, slaPeriod);
        Long realTraffic = checkUnits * trafficPerCheckUnit.get();
        return realTraffic;
    }

    private String getAccessTool(List<Arrangement> arrangements) {
        return arrangements.get(0).getAccessTool();
    }

    private Long getCheckUnitsInPeriod(List<Arrangement> arrangements, Schedule schedule, SlaPeriod slaPeriod) {
        AtomicLong result = new AtomicLong();
        arrangements.stream().forEach(arrangement -> {
            switch (slaPeriod) {
                case DAY:
                    if (schedule.getPlannedDate().getDayOfMonth() == LocalDate.now().getDayOfMonth())
                        result.getAndIncrement();
                    break;
                case MONTH:
                    if (schedule.getPlannedDate().getMonth() == LocalDate.now().getMonth())
                        result.getAndIncrement();
                    break;
            }
        });
        return result.get();
    }

    private void writeDescriptionToDb(Schedule schedule, String description) {
        if (!description.isEmpty()) {
            schedule.setDescription(description);
            scheduleRepo.save(schedule);
        }
    }
}
