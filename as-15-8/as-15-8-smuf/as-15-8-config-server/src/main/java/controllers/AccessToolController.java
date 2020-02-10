package controllers;

import accessTools.AccessToolDTO;
import com.fasterxml.jackson.annotation.JsonView;
import enums.AccessToolParameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Robot;
import model.RobotProperty;
import model.enums.RobotStatus;
import model.Views;
import model.enums.RobotType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import repositories.RobotPropertyRepo;
import repositories.RobotRepository;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AccessToolController {

    private final RobotRepository robotRepository;
    private final RobotPropertyRepo robotPropertyRepo;

    @PostMapping("/access_tools")
    @PreAuthorize("hasAnyRole('ROLE_MANAGE_ARRANGEMENT')")
    @JsonView(Views.AccessTool.class)
    public List<Robot> getAccessTools(){
        return robotRepository.findAllByStatus(RobotStatus.WORK);
    }

    /**
     * Получение робота по имени
     */
    @PostMapping("access_tool_id")
    @PreAuthorize("hasRole('ROLE_SYSTEM')")
    @JsonView(Views.Brief.class)
    public List<Robot> findByName(@RequestParam String name) {
        return robotRepository.findByName(name);
    }

    /**
     * Получение информации о роботе по имени
     * @param name имя робота
     * @return объект с информацией о роботе
     */
    @PostMapping("/access_tool_info")
    @PreAuthorize("hasRole('ROLE_SYSTEM')" )
    public AccessToolDTO getOrigNameAndUrl(@RequestParam String name){
        List<Robot> robots = robotRepository.findByName(name);
        if(robots.size()!=1){
            throw new IllegalArgumentException("Ошибка получения свойств робота! По заданному имени робота " + name +
                " получен не 1 робот. Размер коллекции: " + robots.size());
        }
        Robot robot = robots.get(0);
        String urlKey;
        if (robot.getType()== RobotType.PS){
            urlKey = AccessToolParameter.SEARCH_SYSTEM_URL.propertyKey();
        } else if (robot.getType() == RobotType.PASD){
            urlKey = AccessToolParameter.STUB_URL.propertyKey();
        } else {
            throw new IllegalArgumentException("Ошибка получения свойств робота! Недопустимый тип робота: " + robot.getType());
        }
        List<RobotProperty> urls = robotPropertyRepo.findByRobotAndKey(robot, urlKey);
        if(urls.size()!=1){
            throw new IllegalArgumentException("Ошибка получения свойств робота! По заданному роботу " + robot.getId() +
                " и ключу: " + urlKey + " получено не 1 свойство. Размер коллекции: " + urls.size());
        }
        return new AccessToolDTO(name, robot.getOrigName(), urls.get(0).getValue());
    }
}
