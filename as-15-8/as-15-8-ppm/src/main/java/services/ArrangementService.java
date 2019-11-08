package services;

import exceptions.AS_15_8_PPM_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Arrangement;
import model.ScheduleCheckUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import repositories.ArrangementRepo;
import repositories.ScheduleCheckUnitRepo;

import java.util.*;

/**
 * Created by san
 * Date: 05.11.2019
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ArrangementService {

    private final ArrangementRepo arrangementRepo;
    private final ScheduleCheckUnitRepo scheduleCheckUnitRepo;

    public void updateArrangementPlanInfo(Arrangement arrangement){
        if(arrangement.getPlannedStartTime()==null || arrangement.getPlannedEndTime() == null){
            throw AS_15_8_PPM_Exception.logAndGet(log, String.format("Ошибка изменения планового времени мероприятия. Некорректные входные параметры: дата начала - %s, дата окончания - %s", arrangement.getPlannedStartTime(), arrangement.getPlannedEndTime()));
        }
        Arrangement updateArrangement =
                arrangementRepo.findById(arrangement.getId())
                        .orElseThrow(() -> new AS_15_8_PPM_Exception("Ошибка изменения планового времени мероприятия. Мероприятие с ИД: " + arrangement.getId() + " не было найдено в БД"));
        updateArrangement.setPlannedStartTime(arrangement.getPlannedStartTime());
        updateArrangement.setPlannedEndTime(arrangement.getPlannedEndTime());
        arrangementRepo.save(updateArrangement);
    }

    public Page<Arrangement> findPage(PageRequest page){
        return arrangementRepo.findAllAvailableArrangements(page);
    }

    public List<Arrangement> findAllAvailableArrangements(){
        return arrangementRepo.findAllAvailableArrangements();
    }

    public Map<Arrangement, TreeSet<ScheduleCheckUnit>> getArrangementCheckUnits(List<Long> arrangementIds){
        Map<Arrangement, TreeSet<ScheduleCheckUnit>> arrangementCheckUnits = new HashMap<>();
        arrangementIds.forEach(arrangementId -> {
            Arrangement arrangement = arrangementRepo.findById(arrangementId)
                    .orElseThrow(() -> new AS_15_8_PPM_Exception("Ошибка создания расписания! Мероприятие не было найдено по ID: " + arrangementId));
            TreeSet<ScheduleCheckUnit> arrangementResults = new TreeSet<>(Comparator.comparingLong(ScheduleCheckUnit::getId));
            arrangementResults.addAll(scheduleCheckUnitRepo.findAllByArrangement(arrangement));
            arrangementCheckUnits.put(arrangement, arrangementResults);
        });
        return arrangementCheckUnits;
    }
}
