package controllers.traffic;

import com.fasterxml.jackson.annotation.JsonView;
import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Views;
import model.traffic.SearchPhrase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.SearchPhraseRepository;
import repositories.helpers.SearchPhraseParams;

@RestController
@RequestMapping(path = "/phrases", produces = MediaType.APPLICATION_JSON_VALUE)
@PreAuthorize("hasAnyRole('ROLE_OPERATOR','ROLE_ADMIN')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class SearchPhraseController {

    private final SearchPhraseRepository phraseRepository;

    @GetMapping
    public Page<SearchPhrase> getAllPhrases(
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "false") boolean containsInTraffic,
            @RequestParam(required = false) Long searchTrafficUnitId) {

        Pageable page = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));
        SearchPhraseParams params = new SearchPhraseParams(
                containsInTraffic, searchTrafficUnitId, query);
        return phraseRepository.searchFor(SearchPhrase.class, params, page);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.CREATED)
    @JsonView(Views.Full.class)
    public SearchPhrase createPhrase(@RequestBody SearchPhrase phrase) {
        return phraseRepository.save(phrase);
    }

    @GetMapping(path = "/{id}")
    @JsonView(Views.Full.class)
    public SearchPhrase getPhraseById(@PathVariable Long id) {
        return phraseRepository.findById(id).orElseThrow(() ->
                new AS_15_8_PPT_Exception("Search phrase was not found by id: " + id));
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @JsonView(Views.Full.class)
    public SearchPhrase updatePhrase(@RequestBody SearchPhrase newPhrase,
                                     @PathVariable("id") SearchPhrase oldPhrase) {
        return phraseRepository.save(merge(newPhrase, oldPhrase));
    }

    @DeleteMapping(path = "/{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deletePhrase(@PathVariable Long id) {
        phraseRepository.deleteById(id);
    }

    private SearchPhrase merge(SearchPhrase newPhrase, SearchPhrase oldPhrase) {
        oldPhrase.setPhrase(newPhrase.getPhrase());
        oldPhrase.setSubtypeId(newPhrase.getSubtypeId());
        return oldPhrase;
    }

}
