package controllers;


import com.fasterxml.jackson.annotation.JsonView;
import enums.AccessToolParameter;
import enums.SortingDirection;
import helpers.SortingHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Robot;
import model.RobotProperty;
import model.Views;
import model.enums.RobotType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.RobotPropertyRepo;
import repositories.RobotRepository;
import services.RobotService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/robots", produces = MediaType.APPLICATION_JSON_VALUE)

public class RobotController {

    private final RobotService robotService;
    private final RobotRepository robotRepository;
    private final RobotPropertyRepo robotPropertyRepo;

    @GetMapping
    @JsonView(Views.Brief.class)
    @PreAuthorize("hasAnyRole('ROLE_MANAGE_CONFIGURATIONS')")
    public Page<Robot> getAll(@RequestParam(required = false) SortingDirection sortingDirection,
                              @RequestParam(required = false) String sortingColumn,
                              @RequestParam(defaultValue = "0") int pageNumber,
                              @RequestParam(defaultValue = "10") int pageSize,
                              @RequestParam(required = false) String query) {

        Pageable page = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));
        return robotService.getByQuery(page, query);
    }

    @GetMapping("{id}")
    @JsonView(Views.Full.class)
    @PreAuthorize("hasAnyRole('ROLE_MANAGE_CONFIGURATIONS')")
    public ResponseEntity<Robot> findById(@PathVariable Long id){
        return robotService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('ROLE_MANAGE_CONFIGURATIONS')")
    public ResponseEntity editRobot(@RequestParam("id") Robot robot, @RequestBody Robot newRobot) {
        Matcher nameMatcher = Pattern.compile("^[a-z,-]+$").matcher(newRobot.getName().toLowerCase());
        if(!nameMatcher.find())
            return ResponseEntity.badRequest().body("Ошибка! Имя робота имеет неверный формат. Допускаются только буквы латинского алфавита и дефис");
        return ResponseEntity.ok(robotService.edit(robot, newRobot));
    }

    @DeleteMapping
    @PreAuthorize("hasAnyRole('ROLE_MANAGE_CONFIGURATIONS')")
    public void deleteRobot(@RequestParam("id") Robot robot){
        robotService.delete(robot);
    }

    @PostMapping("/orig_name")
    @PreAuthorize("hasRole('ROLE_SYSTEM')" )
    public Map<String, String> getOrigNameAndUrl(@RequestParam String name){
        Map<String, String> result = new HashMap<>();
        List<Robot> robots = robotRepository.findByName(name);
        if(robots.size()!=1){
            throw new IllegalArgumentException("Ошибка получения свойств робота! По заданному имени робота " + name +
                " получен не 1 робот. Размер коллекции: " + robots.size());
        }
        Robot robot = robots.get(0);
        String urlKey = "";
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
        result.put(robot.getOrigName(), urls.get(0).getValue());
        return result;

    }
}
