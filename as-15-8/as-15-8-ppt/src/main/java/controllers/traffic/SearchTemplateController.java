package controllers.traffic;

import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.traffic.SearchQueryTrafficUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.SearchQueryTrafficUnitRepository;
import repositories.helpers.SearchTemplateParams;

@RestController
@RequestMapping(path = "/templates", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class SearchTemplateController {

    private final SearchQueryTrafficUnitRepository searchQueryTrafficUnitRepository;

    @GetMapping
    public Page<SearchQueryTrafficUnit> getAllTemplates(
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "true") boolean containsInTraffic,
            @RequestParam(required = false) Long trafficId) {

        Pageable page = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));
        SearchTemplateParams params = new SearchTemplateParams(
                containsInTraffic, trafficId, query);
        return searchQueryTrafficUnitRepository.searchFor(SearchQueryTrafficUnit.class, params, page);
    }

    @PostMapping
    public SearchQueryTrafficUnit createSearchQueryTrafficUnit(@RequestBody SearchQueryTrafficUnit searchQueryTrafficUnit){
        return searchQueryTrafficUnitRepository.save(searchQueryTrafficUnit);
    }

}
