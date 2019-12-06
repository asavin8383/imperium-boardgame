package controllers;

import controllers.entity.PS;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Microservice;
import model.Robot;
import model.RobotType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.RobotRepository;
import services.ConfigurationsService;

import java.util.Arrays;
import java.util.HashSet;
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
@PreAuthorize("hasRole('ROLE_SYSTEM')")
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PSController
{
    private final RobotRepository robotRepository;
    private final ConfigurationsService configurationsService;


    /**
     * Прием новых записей о ПС
     * @param data
     */
    @PostMapping("ps")
    void uploadPS(@RequestBody List<PS> data) {
        log.info("Got {} robot records POSTed, trying to insert", data.size());
        insert(data, RobotType.PS);
    }

    /**
     * Прием новых записей о ПАСД
     * @param data
     */
    @PostMapping("pasd")
    void uploadPASD(@RequestBody List<PS> data) {
        log.info("Got {} robot records POSTed, trying to insert", data.size());
        insert(data, RobotType.PASD);
    }

    /**
     * Уданеие ПС
     * @param id
     */
    @DeleteMapping("ps")
    void deletePS(@RequestParam Long id) {
        log.info("Got request, trying to delete PS with id {}", id);
        delete(id, RobotType.PS);
    }

    /**
     * Удаление ПАСД
     * @param id
     */
    @DeleteMapping("pasd")
    void deletePASD(@RequestParam Long id) {
        log.info("Got request, trying to delete PASD with id {}", id);
        delete(id, RobotType.PASD);
    }

    private void insert(@RequestBody List<PS> data, RobotType robotType) {
        Set<Long> all = robotRepository.findAllByType(robotType)
                .stream()
                .map(Robot::getOrigId)
                .collect(Collectors.toSet());
        log.debug("{} robot records already exists");
        int newCnt=0;
        for (PS ps : data) {
            boolean exists = all.contains(ps.getId());
            if (!exists) {
                log.debug("new robot record arrived: {}", ps);
                Robot newRobot = new Robot();
                newRobot.setOrigId(ps.getId());
                newRobot.setOrigName(ps.getName());
                newRobot.setName(ps.getName() + "-" + ps.getId());
                newRobot.setType(robotType);
                newRobot.setConfigurations(new HashSet<>(Arrays.asList(
                        configurationsService.getOrCreate(Microservice.executor),
                        configurationsService.getOrCreate(Microservice.ppm)
                )));
                robotRepository.save(newRobot);
                newCnt++;
            }
        }
        log.info("{} new robot records inserted", newCnt);
    }

    private void delete(Long id, RobotType robotType) {
        robotRepository.deleteByOrigIdAndType(id, robotType);
    }

}
