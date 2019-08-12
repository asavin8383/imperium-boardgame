package services.shedule;

import checkUnits.CheckMethod;
import lombok.RequiredArgsConstructor;
import model.schedule.PlannedProcessingTime;
import model.task.Arrangement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.ArrangementResultRepository;
import repositories.PlannedProcessingTimeRepo;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * Creation date: 08.08.2019
 * Author: asavin
 */
@Service
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class ScheduleService {
    private final PlannedProcessingTimeRepo plannedProcessingTimeRepo;
    private final ArrangementResultRepository arrangementResultRepo;

    /**
     * Расчет времени мероприятия на один обработчик
     * @param arrangement Мероприятие
     * @return время мероприятия в секундах
     */
    public long countArrangementTime(Arrangement arrangement){

        Map<CheckMethod, Long> plannedProcessingTimes =
            plannedProcessingTimeRepo.findAllByAccessTool(arrangement.getAccessTool().getId())
                .stream()
                .collect(Collectors.toMap(PlannedProcessingTime::getCheckMethod, PlannedProcessingTime::getPlannedProcessingTimeMs));
        return arrangementResultRepo.findAllByArrangement(arrangement.getId())
            .stream()
            .mapToLong(arrangementResult -> plannedProcessingTimes.get(arrangementResult.getCheckUnitType().getCheckMethod()))
            .sum();

    }
}
