package controllers;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Robot;
import model.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import repositories.RobotRepository;

import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class AccessToolController {

    private final RobotRepository robotRepository;

    @GetMapping("/access_tools")
    @PreAuthorize("hasAnyRole('ROLE_OPERATOR')")
    @JsonView(Views.AccessTool.class)
    public List<Robot> getAccessTools(){
        return robotRepository.findAll();
    }
}
