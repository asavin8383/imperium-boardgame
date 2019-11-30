package controllers.traffic;

import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.enums.TrafficUnitType;
import model.traffic.SearchQueryTrafficUnit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.SearchPhraseRepository;
import repositories.SearchQueryTrafficUnitRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@RestController
@RequestMapping(path = "/traffic/unit/query",
        produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class SearchQueryTrafficUnitController {

    private final SearchQueryTrafficUnitRepository searchQueryTrafficUnitRepository;
    private final SearchPhraseRepository searchPhraseRepository;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    private SearchQueryTrafficUnit createTemplate(@RequestBody SearchQueryTrafficUnit unit) {
        return searchQueryTrafficUnitRepository.save(unit);
    }

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

    /*@PutMapping(path = "/{id}")
    public SearchQueryTrafficUnit updateTemplate(@PathVariable("id") SearchQueryTrafficUnit existing,
                                                 @RequestBody SearchQueryTrafficUnit changed) {
        existing.setQueryPattern(changed.getQueryPattern());
        update(existing.getFormalErdiList(), changed.getFormalErdiList());
        update(existing.getCustomErdiList(), changed.getCustomErdiList());
        update(existing.getSearchPhrases(), changed.getSearchPhrases());
        return searchQueryTrafficUnitRepository.save(existing);
    }*/

    private <T> void update(Set<T> existing, Set<T> changed) {
        if (changed == null){
            existing.clear();
        } else {
            existing.addAll(changed);

            for (T t : existing) {
                if (!changed.contains(t))
                    existing.remove(t);
            }
        }
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteTemplate(@PathVariable Long id) {
        Optional<SearchQueryTrafficUnit> optional = searchQueryTrafficUnitRepository.findById(id);
        if (optional.isPresent()) {
            SearchQueryTrafficUnit unit = optional.get();
            if (unit.getType() == TrafficUnitType.TEMPLATE)
                searchQueryTrafficUnitRepository.delete(unit);
            else
                throw new UnsupportedOperationException(
                        "Only SearchQueryTrafficUnit with type=TEMPLATE can be deleted");
        } else {
            throw new AS_15_8_PPT_Exception("SearchQueryTrafficUnit not found by id=" + id);
        }
    }

    /*@PutMapping(path = "/{id}/add", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addSearchPhrases(@PathVariable("id") SearchQueryTrafficUnit unit, @RequestBody List<Long> phraseIds) {
        if (unit == null)
            throw new AS_15_8_PPT_Exception("SearchQueryTrafficUnit not found");
        if (unit.getType() != TrafficUnitType.PHRASE)
            throw new AS_15_8_PPT_Exception("Cannot add phrases to SearchQueryTrafficUnit with id=" + unit.getId());

        unit.getSearchPhrases().addAll(
                searchPhraseRepository.findAllById(phraseIds));
        searchQueryTrafficUnitRepository.save(unit);
    }

    @PutMapping(path = "/{id}/remove", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void removeCustomErdi(@PathVariable("id") SearchQueryTrafficUnit unit, @RequestBody List<Long> phraseIds) {
        if (unit == null)
            throw new AS_15_8_PPT_Exception("SearchQueryTrafficUnit not found");
        if (unit.getType() != TrafficUnitType.PHRASE)
            throw new AS_15_8_PPT_Exception("Cannot remove phrases from SearchQueryTrafficUnit with id=" + unit.getId());

        unit.getSearchPhrases().removeAll(
                searchPhraseRepository.findAllById(phraseIds));
        searchQueryTrafficUnitRepository.save(unit);
    }*/

}
