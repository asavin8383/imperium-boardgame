package controllers;

import accessTools.AccessToolDTO;
import com.fasterxml.jackson.annotation.JsonView;
import enums.AccessToolParameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Robot;
import model.RobotProperty;
import model.Views;
import model.enums.RobotStatus;
import model.enums.RobotType;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
        Robot robot = getRobotByName(name);
        AccessToolParameter urlParam;
        if (robot.getType()== RobotType.PS){
            urlParam = AccessToolParameter.SEARCH_SYSTEM_URL;
        } else if (robot.getType() == RobotType.PASD){
            urlParam = AccessToolParameter.STUB_URL;
        } else {
            throw new IllegalArgumentException("Ошибка получения свойств робота! Недопустимый тип робота: " + robot.getType());
        }
        List<RobotProperty> urls = robotPropertyRepo.findByRobotAndKey(robot, urlParam);
        if(urls.size()!=1){
            throw new IllegalArgumentException("Ошибка получения свойств робота! По заданному роботу " + robot.getId() +
                " и ключу: " + urlParam + " получено не 1 свойство. Размер коллекции: " + urls.size());
        }
        return new AccessToolDTO(name, robot.getOrigName(), getPropertyValue(robot, urlParam));
    }

    @PostMapping("/ps_search_query_url")
    @PreAuthorize("hasRole('ROLE_MANAGE_ARRANGEMENT')")
    public ResponseEntity<String> getSearchQueryUrl(@RequestParam String name){
        try {
            Robot robot = getRobotByName(name);
            if (robot.getType()!= RobotType.PS){
                throw new IllegalArgumentException("Ошибка получения ссылки для ПС! переданный робот не является ПС: " + name);
            }
            String psUrl = getPropertyValue(robot, AccessToolParameter.SEARCH_SYSTEM_URL);
            if(Strings.isEmpty(psUrl)){
                return ResponseEntity.noContent().build();
            }
            String psSearchQuery = getPropertyValue(robot, AccessToolParameter.SEARCH_SYSTEM_SEARCH_QUERY);
            if(Strings.isEmpty(psSearchQuery)){
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(psUrl+psSearchQuery);
        } catch (IllegalArgumentException ex){
            log.error("Ошибка получения ссылки!", ex);
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    private Robot getRobotByName(String name){
        List<Robot> robots = robotRepository.findByName(name);
        if(robots.size()!=1){
            throw new IllegalArgumentException("Ошибка получения свойств робота! По заданному имени робота " + name +
                " получен не 1 робот. Размер коллекции: " + robots.size());
        }
        return robots.get(0);
    }

    private String getPropertyValue(Robot robot, AccessToolParameter parameter){
        List<RobotProperty> properties = robotPropertyRepo.findByRobotAndKey(robot, parameter);
        if(properties.size()==0){
            throw new IllegalArgumentException("Ошибка получения свойств робота! По заданному роботу " + robot.getId() +
                " и ключу: " + parameter + " не найдено ни одно значение");
        }
        if(properties.size()>1){
            throw new IllegalArgumentException("Ошибка получения свойств робота! По заданному роботу " + robot.getId() +
                " и ключу: " + parameter + " получено не 1 свойство. Размер коллекции: " + properties.size());
        }
        return properties.get(0).getValue();
    }
}
