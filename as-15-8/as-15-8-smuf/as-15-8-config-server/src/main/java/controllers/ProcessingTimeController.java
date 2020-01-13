package controllers;

import exceptions.AS_15_8_Config_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.ProcessingTime;
import model.Robot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.ProcessingTimeRepository;
import repositories.RobotRepository;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/processing_times", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_MANAGE_CONFIGURATIONS')")
public class ProcessingTimeController {

    private final ProcessingTimeRepository ptRepo;
    private final RobotRepository robotRepo;

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void editProcessingTime(@RequestParam("id") Optional<ProcessingTime> processingTime, @RequestBody ProcessingTime newProcessingTime) {
        processingTime.orElseThrow(() -> new AS_15_8_Config_Exception("Невозможно найти такой processingTime"));

        processingTime.get().setProcessing_time(newProcessingTime.getProcessing_time());
        processingTime.get().setCheck_method(newProcessingTime.getCheck_method());
        processingTime.get().setTraffic_per_check_unit(newProcessingTime.getTraffic_per_check_unit());

        ptRepo.save(processingTime.get());
    }

    @PostMapping("/add")
    public void putProcessingTime(@RequestParam("id") Long robotId, @RequestBody ProcessingTime processingTime){
        robotRepo.findById(robotId).orElseThrow(() -> new AS_15_8_Config_Exception("Невозможно найти робота с таким id: " + robotId));
        Robot robot = robotRepo.findById(robotId).get();

        processingTime.setRobot_id(robot.getId());
        processingTime.setRobot(robot);

        ptRepo.save(processingTime);
    }

    @PostMapping
    public List<ProcessingTime> getProcessingTimes(@RequestParam("id") Long robotId){
        ptRepo.findAllByRobotId(robotId).orElseThrow(() -> new AS_15_8_Config_Exception("Невозможно найти робота с таким id: " + robotId));
        return ptRepo.findAllByRobotId(robotId).get();
    }

    @DeleteMapping
    public void deleteProcessingTime(@RequestParam("id") Optional<ProcessingTime> processingTime){
        processingTime.orElseThrow(() -> new AS_15_8_Config_Exception("Невозможно найти processingTime"));
        ptRepo.delete(processingTime.get());
    }
}
