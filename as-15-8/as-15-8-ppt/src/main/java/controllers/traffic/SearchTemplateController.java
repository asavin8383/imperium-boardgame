package controllers.traffic;

import com.fasterxml.jackson.annotation.JsonView;
import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Views;
import model.traffic.SearchQueryTrafficUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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

    private final SearchQueryTrafficUnitRepository repository;

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
        return repository.searchFor(SearchQueryTrafficUnit.class, params, page);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(Views.Full.class)
    public SearchQueryTrafficUnit createSearchTemplate(@RequestBody SearchQueryTrafficUnit template) {
        return repository.save(template);
    }

    @GetMapping(path = "/{id}")
    @JsonView(Views.Full.class)
    public SearchQueryTrafficUnit getTemplateById(@PathVariable("id") SearchQueryTrafficUnit unit) {
        return unit;
    }

    @PutMapping(path = "/{id}")
    @JsonView(Views.Full.class)
    public SearchQueryTrafficUnit updateTemplate(@RequestBody SearchQueryTrafficUnit newUnit,
                                                 @PathVariable("id") SearchQueryTrafficUnit oldUnit) {
        oldUnit.setQueryPattern(newUnit.getQueryPattern());
        oldUnit.setCategory(newUnit.getCategory());
        oldUnit.setFormalErdiList(newUnit.getFormalErdiList());
        oldUnit.setCustomErdiList(newUnit.getCustomErdiList());
        oldUnit.setSearchPhrases(newUnit.getSearchPhrases());

        return repository.save(oldUnit);
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deletePhrase(@PathVariable Long id) {
        repository.deleteById(id);
    }

}
