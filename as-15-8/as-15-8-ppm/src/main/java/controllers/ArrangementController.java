package controllers;

import arrangement.ArrangementStatusNotification;
import checkUnits.CheckUnit;
import checkUnits.CheckUnitType;
import common.SchedulerProperties;
import enums.AccessToolUnit;
import enums.ArrangementEvents;
import enums.Protocol;
import enums.SortingDirection;
import exceptions.AS_15_8_PPM_Exception;
import helpers.SortingHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Arrangement;
import model.ScheduleCheckUnit;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.ArrangementRepo;
import repositories.ScheduleCheckUnitRepo;
import repositories.ScheduleRepo;
import restapi.ArrangementStatusUploader;
import restapi.pmk.ResultsDownloader;
import restapi.pod.DomainMaskUploader;
import services.ScheduleService;
import webClients.PptWebClient;

import javax.transaction.Transactional;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by san
 * Date: 31.10.2019
 */
@RestController
@RequestMapping(path = "/arrangements", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_SYSTEM')")
@RequiredArgsConstructor(onConstructor_={@Autowired})
@Slf4j
public class ArrangementController {

    private final ArrangementRepo arrangementRepo;
    private final PptWebClient PPTWebClient;
    private final ArrangementStatusUploader arrangementStatusUploader;
    private final SchedulerProperties schedulerProperties;
    private final DomainMaskUploader domainMaskUploader;
    private final ScheduleRepo scheduleRepo;
    private final ScheduleService scheduleService;
    private final ScheduleCheckUnitRepo scheduleCheckUnitRepo;
    private final ResultsDownloader resultsDownloader;

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<String> updateArrangement(@RequestBody Arrangement newArrangement, @RequestParam("id") Arrangement arrangement) {
        try {
            log.info("Получено мероприятие {} для включения в расписание", newArrangement.getId());
            if (arrangement != null) {
                //Удаляем старое мероприятие
                arrangementRepo.delete(arrangement);
                log.info("Мероприятие {} удалено при замене", arrangement.getId());
            }
            if (newArrangement.getPlannedStartTime() == null) {
                newArrangement.setPlannedStartTime(LocalTime.of(9, 0));
            }
            if (newArrangement.getPlannedEndTime() == null) {
                newArrangement.setPlannedEndTime(LocalTime.of(18, 0));
            }
            newArrangement.setMaxWorkersCount(schedulerProperties.getTotalWorkersCount());
            getAndSaveArrangementCheckUnits(newArrangement);
            log.info("Мероприятие {} готово к включению в расписание", newArrangement.getId());
            ArrangementStatusNotification arrangementStatusNotification = new ArrangementStatusNotification(newArrangement.getId(), ArrangementEvents.FILL);
            arrangementStatusUploader.changeArrangementStatus(arrangementStatusNotification);
            return ResponseEntity.ok().build();
        } catch (IllegalStateException ex) {
            log.error("Ошибка при формировании мероприятия", ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        } catch (Exception ex) {
            log.error("Ошибка при формировании мероприятия", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        }
    }

    @PutMapping("/close")
    @Transactional
    public void updateArrangementStatus(@RequestParam("id") Arrangement arrangement){
        if (arrangement == null){
            throw new AS_15_8_PPM_Exception("Ошибка закрытия мероприятия! Мероприятие не было найдено в БД");
        }
        arrangement.setClosed(true);
        arrangementRepo.save(arrangement);
        scheduleService.checkAndCloseSchedule(
            scheduleRepo.findByArrangement(arrangement.getId())
                .orElseThrow(() -> new AS_15_8_PPM_Exception("Ошибка проверки закрытия расписания! Расписание не найдено по ИД мероприятия: " + arrangement.getId()))
        );
    }

    private void getAndSaveArrangementCheckUnits(Arrangement arrangement){
        arrangement.getScheduleCheckUnits().addAll(
                Objects.requireNonNull(
                        PPTWebClient
                                .getCheckUnitsByArrangementId(arrangement.getId())
                                .stream()
                                .flatMap(checkUnit -> createCheckUnits(arrangement, checkUnit).stream())
                                .collect(Collectors.toList())
                )
        );
        log.info("Получен список чек-юнитов мероприятия {} от ППТ", arrangement.getId());
        if(arrangement.getScheduleCheckUnits().size() > 0){
            arrangementRepo.save(arrangement);
            log.info("Список чек-юнитов мероприятия {} сохранен", arrangement.getId());
        } else {
            throw new IllegalStateException("Ошибка формирования мероприятия " + arrangement.getId() +
                    "  в ППМ. Трафик не содержит значений для проверки");
        }
    }

    private List<ScheduleCheckUnit> createCheckUnits(Arrangement arrangement, CheckUnit checkUnit){
        String unitName = schedulerProperties.getAccessTools().get(arrangement.getAccessTool());
        if(Strings.isEmpty(unitName))
            throw new AS_15_8_PPM_Exception("Ошибка при получении конфигурации робота " + arrangement.getAccessTool() + ". Проверьте и обновите конфигурацию ППМ");
        AccessToolUnit accessToolUnit = AccessToolUnit.fromPropertyKey(unitName);
        boolean isPS = accessToolUnit == AccessToolUnit.SEARCH_SYSTEM ||
            accessToolUnit == AccessToolUnit.GOOGLE_API;
        List<ScheduleCheckUnit> scheduleCheckUnits = new ArrayList<>();
        switch (checkUnit.getType()){
            case DOMAIN: {
                addDomainToScheduleCheckUnits(isPS, scheduleCheckUnits, checkUnit.getContentId(), checkUnit.getValue(), arrangement);
                return scheduleCheckUnits;
            }
            case DOMAIN_MASK: {
                domainMaskUploader.getDomains(checkUnit.getValue())
                    .forEach(domain ->
                        addDomainToScheduleCheckUnits(isPS, scheduleCheckUnits, checkUnit.getContentId(), domain, arrangement));
                return scheduleCheckUnits;
            }
            case IP_V4:
            case IP_V6:
            case IP_V4_SUBNET:
            case IP_V6_SUBNET:
            {
                if (!isPS){
                    scheduleCheckUnits.add(createCheckUnit(arrangement, checkUnit.getContentId(), checkUnit.getType(), checkUnit.getValue()));
                }
                return scheduleCheckUnits;
            }
            default: {
                scheduleCheckUnits.add(createCheckUnit(arrangement, checkUnit.getContentId(), checkUnit.getType(), checkUnit.getValue()));
                return scheduleCheckUnits;
            }
        }
    }

    private void addDomainToScheduleCheckUnits(boolean isPS, List<ScheduleCheckUnit> scheduleCheckUnits, Long contentId, String checkUnitValue, Arrangement arrangement){
        if (isPS) {
            scheduleCheckUnits.add(createCheckUnit(arrangement, contentId, CheckUnitType.DOMAIN, checkUnitValue));
        } else {
            for(Protocol value: Protocol.values()){
                scheduleCheckUnits.add(createCheckUnit(arrangement, contentId, CheckUnitType.URL, value.getProtocol() + checkUnitValue));
            }
        }
    }

    private ScheduleCheckUnit createCheckUnit(Arrangement arrangement, Long erdiId, CheckUnitType checkUnitType, String checkUnitValue){
        ScheduleCheckUnit scheduleCheckUnit = new ScheduleCheckUnit();
        scheduleCheckUnit.setArrangement(arrangement);
        scheduleCheckUnit.setErdiId(erdiId);
        scheduleCheckUnit.setCheckUnitType(checkUnitType);
        scheduleCheckUnit.setCheckUnitValue(checkUnitValue);
        return scheduleCheckUnit;
    }

    @GetMapping(path = "/completion")
    public int getArrangementCompletion(@RequestParam("id") Optional<Arrangement> arrangement){
        arrangement.orElseThrow(() -> new AS_15_8_PPM_Exception("Ошибка поиска! Такого поручения не существует."));
        List<ScheduleCheckUnit> checkUnits = scheduleCheckUnitRepo.findAllByArrangement(arrangement.get());

        if (checkUnits == null)
            throw new AS_15_8_PPM_Exception("Ошибка расчёта процента выполнения мероприятия. checkUnits null");

        Long notPlannedNotRunning = resultsDownloader.getNotPlannedNotRunningResults(arrangement.get().getId());
        if (notPlannedNotRunning == null)
            throw new AS_15_8_PPM_Exception("Ошибка расчёта процента выполнения мероприятия. Число результатов not RUNNING и PLANNED null");

        int percent = (int) ((notPlannedNotRunning* 100)/checkUnits.size());
        return percent;
    }

}
