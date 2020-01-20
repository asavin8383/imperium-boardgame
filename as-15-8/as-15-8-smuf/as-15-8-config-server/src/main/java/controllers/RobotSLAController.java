package controllers;

import com.fasterxml.jackson.annotation.JsonView;
import exceptions.AS_15_8_Config_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Robot;
import model.RobotSLA;
import model.Views;
import model.enums.RobotType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.RobotSLARepository;

import java.util.List;
import java.util.Optional;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/robots_sla", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_MANAGE_CONFIGURATIONS')")
public class RobotSLAController {

    private final RobotSLARepository robotSLARepo;

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity editRobotSla(@RequestParam("id") Optional<RobotSLA> robotSLA, @RequestBody RobotSLA newRobotSLA) {
        robotSLA.orElseThrow(() -> new AS_15_8_Config_Exception("Невозможно найти такое SLA"));

        if (robotNotPASDType(newRobotSLA.getRobot()))
            return ResponseEntity.badRequest().body("Недопустимая операция! Только робот ПАСД может иметь настройки SLA");

        robotSLA.get().setSlaPeriod(newRobotSLA.getSlaPeriod());
        robotSLA.get().setSlaType(newRobotSLA.getSlaType());
        robotSLA.get().setCheckUnitValue(newRobotSLA.getCheckUnitValue());

        robotSLARepo.save(robotSLA.get());
        return ResponseEntity.ok("Редактирование успешно");
    }

    @PostMapping("/add")
    public ResponseEntity putRobotSla(@RequestParam("id") Optional<Robot> robot, @RequestBody RobotSLA robotSLA){
        robot.orElseThrow(() -> new AS_15_8_Config_Exception("Невозможно найти робота с таким id"));

        if (robotNotPASDType(robot.get()))
            return ResponseEntity.badRequest().body("Недопустимая операция! Только робот ПАСД может иметь настройки SLA");

        robotSLA.setRobot(robot.get());
        robotSLARepo.save(robotSLA);
        return ResponseEntity.ok("Добавление SLA роботу прошло успешно");
    }

    @PostMapping
    public List<RobotSLA> getRobotSla(@RequestParam("id") Optional<Robot> robot) {
        robot.orElseThrow(() -> new AS_15_8_Config_Exception("Невозможно найти робота с таким id"));
        return robotSLARepo.findAllByRobotId(robot.get().getId()).get();
    }

    @DeleteMapping
    public void deleteRobotSla(@RequestParam("id") Optional<RobotSLA> robotSLA){
        robotSLA.orElseThrow(() -> new AS_15_8_Config_Exception("Невозможно найти robotSLA"));
        robotSLARepo.delete(robotSLA.get());
    }

    @PostMapping("/all")
    @JsonView(Views.Sla.class)
    public List<RobotSLA> getRobotsSla() {
        return robotSLARepo.findAll();
    }

    private boolean robotNotPASDType(Robot robot) {
        return robot.getType() != RobotType.PASD;
    }
}
