package controllers.traffic;

import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.enums.TrafficUnitType;
import model.traffic.SearchQueryPattern;
import model.traffic.SearchQueryTrafficUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.SearchQueryPatternRepo;
import repositories.SearchQueryTrafficUnitRepository;
import services.traffic.TrafficService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping(path = "/traffic/unit/query",
        produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasRole('ROLE_PREPARATION_TRAFFIC')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class SearchQueryTrafficUnitController {

    private final SearchQueryTrafficUnitRepository searchQueryTrafficUnitRepository;
    private final SearchQueryPatternRepo searchQueryPatternRepo;
    private final TrafficService trafficService;

    @GetMapping(path = "/{id}")
    public SearchQueryTrafficUnit getTemplate(@PathVariable Long id) {
        Optional<SearchQueryTrafficUnit> optional = searchQueryTrafficUnitRepository.findById(id);
        if (optional.isPresent()) {
            SearchQueryTrafficUnit unit = optional.get();
            if (unit.getType() == TrafficUnitType.TEMPLATE)
                return unit;
            else
                throw new AS_15_8_PPT_Exception("Only units with type=TEMPLATE can be returned");
        } else {
            throw new AS_15_8_PPT_Exception("SearchQueryTrafficUnit not found by id=" + id);
        }
    }

    @PutMapping(path = "/{id}/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SearchQueryTrafficUnit addPatternsToTemplate(@PathVariable("id") SearchQueryTrafficUnit existing,
                                                 @RequestBody List<SearchQueryPattern> searchQueryPatterns) {
        if(existing == null){
            throw new AS_15_8_PPT_Exception("Ошибка добавления шаблонов в трафик-юнит! трафик-юнит не найден в БД");
        }
        existing.getSearchQueryPatterns().addAll(searchQueryPatterns);
        SearchQueryTrafficUnit searchQueryTrafficUnit = searchQueryTrafficUnitRepository.save(existing);
        trafficService.actualizeTraffic(searchQueryTrafficUnit.getTraffic().getId());
        return searchQueryTrafficUnit;
    }

    @PutMapping(path = "/{id}/remove", consumes = MediaType.APPLICATION_JSON_VALUE)
    public SearchQueryTrafficUnit removePatternsFroTemplate(@PathVariable("id") SearchQueryTrafficUnit existing,
                               @RequestBody List<SearchQueryPattern> searchQueryPatterns) {
        if(existing == null){
            throw new AS_15_8_PPT_Exception("Ошибка добавления шаблонов в трафик-юнит! трафик-юнит не найден в БД");
        }
        existing.getSearchQueryPatterns().removeAll(searchQueryPatterns);
        SearchQueryTrafficUnit searchQueryTrafficUnit = searchQueryTrafficUnitRepository.save(existing);
        trafficService.actualizeTraffic(searchQueryTrafficUnit.getTraffic().getId());
        return searchQueryTrafficUnit;
    }

    @GetMapping(path = "/{id}/search_query_patterns")
    public Page<SearchQueryPattern> getTrafficUnitPatterns(
            @PathVariable("id") SearchQueryTrafficUnit searchQueryTrafficUnit,
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize){
        if (searchQueryTrafficUnit == null) {
            throw new AS_15_8_PPT_Exception("Ошибка получения шаблонов трафик-юнита! Трафик-юнит не найден в БД");
        }
        PageRequest page = PageRequest.of(
                pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
        return searchQueryPatternRepo.findAllBySearchQueryTrafficUnits(searchQueryTrafficUnit, page);
    }

}
