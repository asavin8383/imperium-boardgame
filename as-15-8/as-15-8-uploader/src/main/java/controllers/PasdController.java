package controllers;

import controllers.utils.SortingDirection;
import controllers.utils.SortingHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.scheme.PasdRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import repositories.PasdRepository;
import utils.Utils;

@RestController
@RequestMapping(path = "/pasd", produces = MediaType.APPLICATION_JSON_VALUE)
//@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class PasdController {

    private final PasdRepository pasdRepository;

    @GetMapping
    public Page<PasdRecord> getPasdPage(@RequestParam(required = false) SortingDirection sortingDirection,
                                        @RequestParam(required = false) String sortingColumn,
                                        @RequestParam(defaultValue = "0") int pageNumber,
                                        @RequestParam(defaultValue = "10") int pageSize,
                                        @RequestParam(required = false) String name,
                                        @RequestParam(required = false) String hostname) {
        Pageable page = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));
        ExampleMatcher matcher = ExampleMatcher.matchingAll()
                .withIgnoreNullValues()
                .withIgnoreCase()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);
        Example<PasdRecord> example = PasdRecord.example(matcher, Utils.getLocalEndDate(), name, hostname);
        return pasdRepository.findAll(example, page);
    }

}
