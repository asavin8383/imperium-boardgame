package controllers;


import com.fasterxml.jackson.annotation.JsonView;
import enums.SortingDirection;
import helpers.SortingHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Robot;
import model.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import services.RobotService;

import java.util.List;


@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/robots", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
public class RobotController {

    private final RobotService robotService;

    @PostMapping
    @JsonView(Views.Brief.class)
    public List<Robot> getAll(@RequestParam(required = false) SortingDirection sortingDirection,
                              @RequestParam(required = false) String sortingColumn,
                              @RequestParam(defaultValue = "0") int pageNumber,
                              @RequestParam(defaultValue = "10") int pageSize) {

        Pageable page = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));
        return robotService.get(page);
    }

    @PostMapping(path = "{id}")
    @JsonView(Views.Brief.class)
    public ResponseEntity<Robot> findById(@PathVariable Long id){
        return robotService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.badRequest().build());
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void editRobot(@RequestParam("id") Robot robot, @RequestBody Robot newRobot) {
        robotService.edit(robot, newRobot);
    }

    @DeleteMapping
    public void deleteRobot(@RequestParam("id") Robot robot){
        robotService.delete(robot);
    }
}
