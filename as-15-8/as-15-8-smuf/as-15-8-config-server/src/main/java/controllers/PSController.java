package controllers;

import controllers.entity.PS;
import lombok.extern.slf4j.Slf4j;
import model.Robot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
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
     * Прием новых записей о ПС/ПАСД
     * Т.к. формат в обоих случаях одинаков, пусть будет один endpoint
     * @param data
     */
    @PostMapping("ps")
    void uploadPS(@RequestBody List<PS> data) {
        log.info("Got {} robot records POSTed, trying to insert", data.size());
        Set<Long> all = robotRepository.findAll().stream().map(Robot::getOrig_id).collect(Collectors.toSet());
        log.debug("{} robot records already exists");
        int newCnt=0;
        for (PS ps : data) {
            boolean exists = all.contains(ps.getId());
            if (!exists) {
                log.debug("new robot record arrived: {}", ps);
                Robot newRobot = new Robot();
                newRobot.setOrig_id(ps.getId());
                newRobot.setOrig_name(ps.getName());
                newRobot.setName(ps.getName() + "-" + ps.getId());
                robotRepository.save(newRobot);
                newCnt++;
            }
        }
        log.info("{} new robot records inserted", newCnt);
    }

}
