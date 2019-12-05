package services;


import enums.AccessToolUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Robot;
import model.RobotProperty;
import model.RobotType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import repositories.RobotRepository;
import javax.persistence.EntityManager;
import java.util.Optional;
import java.util.Set;


@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class RobotService {

    private final RobotRepository robotRepository;

    private final EntityManager em;

    public void editRobot(Robot robot){
        log.info("Сохранение робота: {}", robot);
        if (robot.getId() == null){
            return;
        }

        Optional<Robot> optRobotOld = robotRepository.findById(robot.getId());
        if (!optRobotOld.isPresent()){
            log.info("Робот не найден в БД, id = {}", robot.getId());
            return;
        }

        Robot robotEdit = optRobotOld.get();

        robotEdit.setName(robot.getName());
        robotEdit.setAccessTool(robot.getAccessTool());
        filterAccessToolParameters(robotEdit.getType(), robotEdit.getAccessTool(), robotEdit.getRobotProperties());

        //robotRepository.save(robotEdit);
        log.info("Робот успешно сохранен: {}", robotEdit);
    }

    private void filterAccessToolParameters(RobotType robotType, AccessToolUnit accessToolUnit, Set<RobotProperty> robotProperties){
        // todo - параметры в зависимости от типа робота
    }

}
