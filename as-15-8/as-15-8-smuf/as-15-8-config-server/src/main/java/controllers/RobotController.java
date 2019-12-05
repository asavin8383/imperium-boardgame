package controllers;


import com.fasterxml.jackson.annotation.JsonView;
import enums.AccessToolUnit;
import enums.SortingDirection;
import helpers.SortingHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Robot;
import model.RobotType;
import model.Views;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import repositories.RobotRepository;
import services.RobotService;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/robots", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
public class RobotController {

    private final RobotRepository robotRepository;
    private final RobotService robotService;

    @PostMapping
    @JsonView(Views.Brief.class)
    public ResponseEntity<Page<Robot>> getSearchSystemPage(@RequestParam(required = false) SortingDirection sortingDirection,
                                                           @RequestParam(required = false) String sortingColumn,
                                                           @RequestParam(defaultValue = "0") int pageNumber,
                                                           @RequestParam(defaultValue = "10") int pageSize,
                                                           @RequestParam(required = false) String query) {

        Pageable page = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));
        Page<Robot> result = robotRepository.findByQuery(query, page);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @PostMapping("{id}")
    @JsonView(Views.Brief.class)
    public Robot findById(@PathVariable Long id){
        Optional<Robot> byId = robotRepository.findById(id);
        return byId.orElseThrow(() -> new HttpClientErrorException(HttpStatus.NOT_FOUND, "Robot not found with id " + id));
    }

    @PostMapping("/access_tools")
    public List<AccessToolUnit> findById(@RequestParam(required = false) RobotType type){
        List<AccessToolUnit> toolALL = Arrays.asList(AccessToolUnit.values());
        List<AccessToolUnit> toolPS = Arrays.asList(AccessToolUnit.SEARCH_SYSTEM);
        List<AccessToolUnit> toolPASD = toolALL.stream()
                .filter(accessToolUnit -> !toolPS.contains(accessToolUnit))
                .collect(Collectors.toList());

        return type == null ? toolALL : (type == RobotType.PS ? toolPS : toolPASD);
    }

    @PutMapping(path = "/edit", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void editRobot(@RequestBody Robot robot) {
        robotService.editRobot(robot);
    }

}
