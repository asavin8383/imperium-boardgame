package controllers;

import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.sor.Violation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import repositories.ViolationRepository;

@RestController
@RequestMapping(path = "/violation", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class ViolationController {

    private final ViolationRepository violationRepository;

    @GetMapping
    public Page<Violation> getViolationPage(@RequestParam(required = false) SortingDirection sortingDirection,
                                            @RequestParam(required = false) String sortingColumn,
                                            @RequestParam(defaultValue = "0") int pageNumber,
                                            @RequestParam(defaultValue = "10") int pageSize,
                                            @RequestParam(required = false) String query,
                                            @RequestParam(required = false) Boolean deleted) {
        Pageable page = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));
        ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<Violation> example = Violation.example(matcher, query, deleted);
        return violationRepository.findAll(example, page);
    }

}
