package controllers;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Robot;
import model.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import repositories.RobotRepository;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AccessToolController {

    private final RobotRepository robotRepository;

    @PostMapping("/access_tools")
    @PreAuthorize("hasAnyRole('ROLE_MANAGE_ARRANGEMENT')")
    @JsonView(Views.AccessTool.class)
    public List<Robot> getAccessTools(){
        return robotRepository.findAll();
    }

    /**
     * Получение робота по имени
     * @param data
     */
    @PostMapping("access_tool_id")
    @PreAuthorize("hasRole('ROLE_SYSTEM')")
    @JsonView(Views.Brief.class)
    public List<Robot> findByName(@RequestParam String name) {
        return robotRepository.findByName(name);
    }
}
