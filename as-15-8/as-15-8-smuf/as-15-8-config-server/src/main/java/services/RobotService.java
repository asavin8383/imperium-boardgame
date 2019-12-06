package services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Robot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import repositories.RobotRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class RobotService {

    private final RobotRepository robotRepository;

    public Page<Robot> get(Pageable pageable){
        return robotRepository.findPage(pageable);
    }

    public Optional<Robot> findById(Long id){
        return robotRepository.findById(id);
    }

    public void edit(Robot robot, Robot newRobot){
        log.info("Сохранение робота: {}", robot);

        if(robot == null)
            throw new IllegalArgumentException("Робот не найден по ID");

        robot.setName(newRobot.getName());
        robot.setAccessTool(newRobot.getAccessTool());
        robot.setModificationDate(LocalDateTime.now());
        robot.setStatus(newRobot.getStatus());

        robot.getRobotProperties().clear();
        newRobot.getRobotProperties().forEach(prop -> {
            prop.setRobot(robot);
            robot.getRobotProperties().add(prop);
        });

        robotRepository.save(robot);
        log.info("Робот успешно сохранен: id {}, name {}", robot.getId(), robot.getName());
    }

    public void delete(Robot robot){
        if(robot.getId() < 0)
            robotRepository.delete(robot);
    }
}
