package services.schedule;

import checkUnits.CheckMethod;
import checkUnits.CheckUnitType;
import lombok.RequiredArgsConstructor;
import model.catalog.AccessTool;
import model.schedule.PlannedProcessingTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.PlannedProcessingTimeRepo;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PlannedProcessingTimeService {

    private final PlannedProcessingTimeRepo plannedProcessingTimeRepo;

    public long getProcessingTime(AccessTool accessTool, CheckUnitType checkUnitType){
        CheckMethod checkMethod = getCheckMethod(checkUnitType);
        return Math.round(plannedProcessingTimeRepo.findAllByAccessTool(accessTool.getId())
                .stream().filter(plannedProcessingTime -> plannedProcessingTime.getCheckMethod().equals(checkMethod))
                .mapToLong(PlannedProcessingTime::getPlannedProcessingTimeMs)
                .findFirst()
                .orElse(0L) / 1000);
    }

    private CheckMethod getCheckMethod(CheckUnitType checkUnitType){
        switch (checkUnitType){
            case IP_V4_SUBNET:
            case IP_V6_SUBNET:
                return CheckMethod.NMAP;
            case URL:
            case DOMAIN:
            case DOMAIN_MASK:
            case IP_V4:
            case IP_V6:
            default:
                return CheckMethod.BROWSER;
        }
    }
}
