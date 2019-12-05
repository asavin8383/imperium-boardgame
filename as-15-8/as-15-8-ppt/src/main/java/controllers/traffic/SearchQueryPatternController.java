package controllers.traffic;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Views;
import model.traffic.CustomErdiView;
import model.traffic.SearchPhrase;
import model.traffic.SearchQueryPattern;
import model.traffic.SearchQueryPatternContentJoin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import repositories.CustomErdiViewRepository;
import repositories.SearchPhraseRepository;
import repositories.SearchQueryPatternContentJoinRepo;
import repositories.SearchQueryPatternRepo;
import webClients.PodWebClient;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by san
 * Date: 26.11.2019
 */
@RestController
@RequestMapping(path = "/traffic/search_query_patterns", produces = MediaType.APPLICATION_JSON_VALUE)
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@PreAuthorize("hasRole('ROLE_PREPARATION_TRAFFIC')")
public class SearchQueryPatternController {

    private final SearchQueryPatternRepo searchQueryPatternRepo;
    private final SearchPhraseRepository searchPhraseRepository;
    private final CustomErdiViewRepository customErdiViewRepository;
    private final SearchQueryPatternContentJoinRepo searchQueryPatternContentJoinRepo;
    private final PodWebClient podWebClient;

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(code = HttpStatus.CREATED)
    public SearchQueryPattern createNewSearchQueryPattern(@RequestBody SearchQueryPattern searchQueryPattern){
        if(searchQueryPattern.getFormalErdiList() != null) {
            searchQueryPattern.getFormalErdiList().forEach(searchQueryPatternContentJoin -> searchQueryPatternContentJoin.setSearchQueryPattern(searchQueryPattern));
        }
        return searchQueryPatternRepo.save(searchQueryPattern);
    }

    @PutMapping
    public SearchQueryPattern updateSearchQueryPattern(
            @RequestParam("id") SearchQueryPattern existingSearchQueryPattern,
            @RequestBody SearchQueryPattern newSearchQueryPattern) {
        if (existingSearchQueryPattern == null){
            if(newSearchQueryPattern.getFormalErdiList() != null) {
                newSearchQueryPattern.getFormalErdiList().forEach(searchQueryPatternContentJoin -> searchQueryPatternContentJoin.setSearchQueryPattern(newSearchQueryPattern));
            }
            return searchQueryPatternRepo.save(newSearchQueryPattern);
        } else {
            existingSearchQueryPattern.setName(newSearchQueryPattern.getName());
            existingSearchQueryPattern.setQueryPattern(newSearchQueryPattern.getQueryPattern());
            newSearchQueryPattern.getFormalErdiList().forEach(searchQueryContentJoin -> searchQueryContentJoin.setSearchQueryPattern(existingSearchQueryPattern));
            update(existingSearchQueryPattern.getFormalErdiList(), newSearchQueryPattern.getFormalErdiList());
            if(existingSearchQueryPattern.getFormalErdiList() != null) {
                //Так как one-to-many, нужно явно связать с родителем
                existingSearchQueryPattern.getFormalErdiList().forEach(searchQueryContentJoin -> searchQueryContentJoin.setSearchQueryPattern(existingSearchQueryPattern));
            }
            update(existingSearchQueryPattern.getCustomErdiList(), newSearchQueryPattern.getCustomErdiList());
            update(existingSearchQueryPattern.getSearchPhrases(), newSearchQueryPattern.getSearchPhrases());
            return searchQueryPatternRepo.save(existingSearchQueryPattern);
        }
    }

    @DeleteMapping
    public void deleteSearchQueryPattern(@RequestParam("id") SearchQueryPattern searchQueryPattern){
        if (searchQueryPattern != null){
            searchQueryPatternRepo.delete(searchQueryPattern);
        }
    }

    @GetMapping("{id}")
    @JsonView(Views.Brief.class)
    public SearchQueryPattern findSearchQueryPattern(@PathVariable("id") SearchQueryPattern searchQueryPattern){
        return searchQueryPattern;
    }

    @GetMapping
    public Page<SearchQueryPattern> findPage(
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String pattern
    ) {
        if (pattern == null) {
            pattern = "";
        }
        PageRequest page = PageRequest.of(
                pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
        return searchQueryPatternRepo.findAllByQueryPatternContaining(pattern, page);
    }

    @GetMapping("{id}/search_phrases")
    public Page<SearchPhrase> findTemplateSearchPhrases(
            @PathVariable("id") SearchQueryPattern searchQueryPattern,
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize,
            @RequestParam(required = false) String phrase){
        if (phrase == null) {
            phrase = "";
        }
        PageRequest page = PageRequest.of(
                pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
        return searchPhraseRepository.findAllBySearchQueryPatternsAndPhraseContaining(searchQueryPattern, phrase, page);
    }

    @GetMapping("{id}/custom_erdi")
    public Page<CustomErdiView> findTemplateFormalErdi(
            @PathVariable("id") SearchQueryPattern searchQueryPattern,
            @RequestParam(required = false) SortingDirection sortingDirection,
            @RequestParam(required = false) String sortingColumn,
            @RequestParam(defaultValue = "0") int pageNumber,
            @RequestParam(defaultValue = "10") int pageSize){
        PageRequest page = PageRequest.of(
                pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
        return customErdiViewRepository.findAllBySearchQueryPatterns(searchQueryPattern, page);
    }

    @GetMapping("{id}/formal_erdi")
    public Page<ObjectNode> getFormalErdiIds(@RequestParam(required = false) SortingDirection sortingDirection,
                                             @RequestParam(required = false) String sortingColumn,
                                             @RequestParam(defaultValue = "0") int pageNumber,
                                             @RequestParam(defaultValue = "10") int pageSize,
                                             @PathVariable("id") SearchQueryPattern searchQueryPattern) {
        Pageable pageable = PageRequest.of(pageNumber, pageSize,
                SortingHelper.createSorting(sortingDirection, sortingColumn));
        if (searchQueryPattern != null) {
            Page<SearchQueryPatternContentJoin> searchQueryPatternContentJoins =
                    searchQueryPatternContentJoinRepo
                            .findAllBySearchQueryPattern(searchQueryPattern, pageable);
            List<Long> contentIds = searchQueryPatternContentJoins
                    .stream()
                    .map(SearchQueryPatternContentJoin::getContentId)
                    .collect(Collectors.toList());
            List<ObjectNode> erdiList = podWebClient.fetchErdi(contentIds);
            return new PageImpl<>(erdiList, pageable, searchQueryPatternContentJoins.getTotalElements());
        } else {
            throw new AS_15_8_PPT_Exception("Not supported");
        }
    }

    private <T> void update(Set<T> existing, Set<T> changed) {
        existing.clear();
        if (changed != null){
            existing.addAll(changed);
        }
    }
}
