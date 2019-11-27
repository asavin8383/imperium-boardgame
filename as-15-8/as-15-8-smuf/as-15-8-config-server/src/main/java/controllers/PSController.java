package controllers;

import com.fasterxml.jackson.annotation.JsonView;
import controllers.entity.PS;
import lombok.extern.slf4j.Slf4j;
import model.Robot;
import model.RobotType;
import model.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.RobotRepository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * User: asinjavin
 * Date: 23.10.2019
 * Time: 18:15
 */
@RestController
@RequestMapping("/")
@Slf4j
public class PSController
{
    private final RobotRepository robotRepository;

    @Autowired
    public PSController(RobotRepository robotRepository) {this.robotRepository = robotRepository;}

    /**
     * Прием новых записей о ПС
     * @param data
     */
    @PostMapping("ps")
    @PreAuthorize("hasRole('ROLE_SYSTEM')")
    void uploadPS(@RequestBody List<PS> data) {
        log.info("Got {} robot records POSTed, trying to insert", data.size());
        insert(data, RobotType.PS);
    }

    /**
     * Прием новых записей о ПАСД
     * @param data
     */
    @PostMapping("pasd")
    @PreAuthorize("hasRole('ROLE_SYSTEM')")
    void uploadPASD(@RequestBody List<PS> data) {
        log.info("Got {} robot records POSTed, trying to insert", data.size());
        insert(data, RobotType.PASD);
    }

    /**
     * Уданеие ПС
     * @param id
     */
    @DeleteMapping("ps")
    @PreAuthorize("hasRole('ROLE_SYSTEM')")
    void deletePS(@RequestParam Long id) {
        log.info("Got request, trying to delete PS with id {}", id);
        delete(id, RobotType.PS);
    }

    /**
     * Удаление ПАСД
     * @param id
     */
    @DeleteMapping("pasd")
    @PreAuthorize("hasRole('ROLE_SYSTEM')")
    void deletePASD(@RequestParam Long id) {
        log.info("Got request, trying to delete PASD with id {}", id);
        delete(id, RobotType.PASD);
    }

    private void insert(@RequestBody List<PS> data, RobotType robotType) {
        Set<Long> all = robotRepository.findAll().stream().map(Robot::getOrigId).collect(Collectors.toSet());
        log.debug("{} robot records already exists");
        int newCnt=0;
        for (PS ps : data) {
            boolean exists = all.contains(ps.getOrigId());
            if (!exists) {
                log.debug("new robot record arrived: {}", ps);
                Robot newRobot = new Robot();
                newRobot.setOrigId(ps.getOrigId());
                newRobot.setOrigName(ps.getName());
                newRobot.setName(ps.getName() + "-" + ps.getOrigId());
                newRobot.setType(robotType);
                robotRepository.save(newRobot);
                newCnt++;
            }
        }
        log.info("{} new robot records inserted", newCnt);
    }

    private void delete(Long origId, RobotType robotType) {
        robotRepository.deleteByOrigIdAndType(origId, robotType);
    }

}
