package controllers;

import enums.SortingDirection;
import helpers.SortingHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Arrangement;
import model.ScheduleCheckUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import repositories.ScheduleCheckUnitRepo;

/**
 * Created by san
 * Date: 09.11.2019
 */
@RestController
@Slf4j
@RequestMapping(path = "/checkunits", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_FORMATION_OF_SHEDULE')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CheckUnitController {

    private final ScheduleCheckUnitRepo scheduleCheckUnitRepo;

    @GetMapping
    public Page<ScheduleCheckUnit> findPage(
            @RequestParam("id") Arrangement arrangement,
            @RequestParam(required = false) String query,
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize){
        PageRequest page = PageRequest.of(
                pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
        return StringUtils.isEmpty(query) ?
                scheduleCheckUnitRepo.findAllByArrangement(arrangement, page) :
                scheduleCheckUnitRepo.findAllByArrangement(arrangement, query, page);
    }

}
