package controllers;

import enums.SortingDirection;
import exceptions.AS_15_8_PPM_Exception;
import helpers.SortingHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Arrangement;
import model.Schedule;
import model.ScheduleCheckUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import repositories.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Created by san
 * Date: 09.11.2019
 */
@RestController
@Slf4j
@RequestMapping(path = "/checkunits", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_FORMATION_OF_SCHEDULE')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CheckUnitController {

    private final ScheduleCheckUnitRepo scheduleCheckUnitRepo;
    private final SchedulePeriodCheckUnitRepo schedulePeriodCheckUnitRepo;
    private final ScheduleRepo scheduleRepo;
    private final ArrangementRepo arrangementRepo;

    @GetMapping
    public Page<ScheduleCheckUnit> findPage(
            @RequestParam("id") Arrangement arrangement,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize){
        PageRequest page = PageRequest.of(
                pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
        return StringUtils.isEmpty(query) ?
                scheduleCheckUnitRepo.findAllByArrangement(arrangement, page) :
                scheduleCheckUnitRepo.findAllByArrangement(arrangement, query, page);
    }

    @GetMapping(path ="/arrangement_analytics")
    public ResponseEntity<List<String>> getArrangementAnalytics(@RequestParam("id") Schedule schedule) {
        if (schedule == null)
            throw new AS_15_8_PPM_Exception("Ошибка анализа запланированных мероприятий для расписания");

        List<Long> arrangementIds = new ArrayList();
        schedule.getSchedulePeriods().forEach(schedulePeriod -> {
            schedulePeriod.getSchedulePeriodArrangements().forEach(schedulePeriodArrangement -> {
                arrangementIds.add(schedulePeriodArrangement.getArrangement().getId());
            });
        });

        List<String> notifications = analyzeArrangement(arrangementIds);
        if (!notifications.isEmpty()) {
            return ResponseEntity.badRequest().body(notifications);
        } else return ResponseEntity.ok().build();

    }

    private List<String> analyzeArrangement(List<Long> arrangementIds) {
        List<String> result = new ArrayList<>();
        arrangementIds.forEach(id-> {
            Optional<Arrangement> arr = arrangementRepo.findById(id);
            Long actualCheckUnits = scheduleCheckUnitRepo.findCheckUnitsCount(arr.get());
            Long spcu = schedulePeriodCheckUnitRepo.getSchedulePeriodCheckUnitCount(id);
            if (spcu < actualCheckUnits) {
                result.add("Внимание! Планировщиком на конец дня для мероприятия id = " + id +
                        " запланировано (и лишь столько будет выполнено): " + spcu +
                        " проверок. В мероприятие внесено " + actualCheckUnits +
                        " проверок. Если вы хотите выполнить мероприятие целиком - следует запланировать мероприятие на следующий день.");
            }
        });
        return result;
    }
}
