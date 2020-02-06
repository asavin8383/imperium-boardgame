package controllers.traffic;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.node.ObjectNode;
import controllers.helpers.SortingHelper;
import enums.SortingDirection;
import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.Views;
import model.traffic.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import repositories.CustomErdiViewRepository;
import repositories.SearchPhraseRepository;
import repositories.SearchQueryPatternContentJoinRepo;
import repositories.SearchQueryPatternRepo;
import webClients.PodWebClient;

import java.time.LocalDate;
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
            /*newSearchQueryPattern.getFormalErdiList().forEach(searchQueryContentJoin -> searchQueryContentJoin.setSearchQueryPattern(existingSearchQueryPattern));
            update(existingSearchQueryPattern.getFormalErdiList(), newSearchQueryPattern.getFormalErdiList());
            if(existingSearchQueryPattern.getFormalErdiList() != null) {
                //Так как one-to-many, нужно явно связать с родителем
                existingSearchQueryPattern.getFormalErdiList().forEach(searchQueryContentJoin -> searchQueryContentJoin.setSearchQueryPattern(existingSearchQueryPattern));
            }
            update(existingSearchQueryPattern.getCustomErdiList(), newSearchQueryPattern.getCustomErdiList());
            update(existingSearchQueryPattern.getSearchPhrases(), newSearchQueryPattern.getSearchPhrases());*/
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

    @PutMapping(path = "/{id}/formal_erdi", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addErdiToPattern(@PathVariable("id") SearchQueryPattern pattern, @RequestBody List<Long> ids) {
        if(pattern==null){
            throw new AS_15_8_PPT_Exception("Ошибка изменения шаблона! Шаблон не найден в БД");
        }
        saveErdi(pattern, ids);
    }

    @PutMapping(path = "/{id}/formal_erdi/remove", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void removeErdiFromPattern(@PathVariable("id") SearchQueryPattern pattern, @RequestBody List<Long> ids) {
        if(pattern==null){
            throw new AS_15_8_PPT_Exception("Ошибка изменения шаблона! Шаблон не найден в БД");
        }
        pattern.getFormalErdiList().removeAll(
            searchQueryPatternContentJoinRepo.findAllBySearchQueryPatternAndContentIdIn(pattern, ids));

        searchQueryPatternRepo.save(pattern);
    }

    @PutMapping(path = "/{id}/formal_erdi/addFromPod")
    public List<Long> addErdiToPatternFromPod(
        @PathVariable("id") SearchQueryPattern pattern,
        @RequestParam(required = false) String idMask,
        @RequestParam(required = false) List<String> categoryNames,
        @RequestParam(required = false) List<String> decisionOrgs,
        @RequestParam(required = false) List<String> infoTypeIds,
        @RequestParam(required = false) List<String> registryNames,
        @RequestParam(required = false) List<String> resourceTypes,
        @RequestParam(required = false) String resourceValue,
        @RequestParam(required = false) List<String> violationNames,
        @RequestParam(required = false) Integer size,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startTime,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endTime,
        @RequestParam(required = false) Boolean random,
        @RequestParam(required = false) SortingDirection sortingDirection,
        @RequestParam(required = false) String sortingColumn,
        @RequestParam(required = false) Long visitorsCntRussiaMin,
        @RequestParam(required = false) Long visitorsCntRussiaMax,
        @RequestParam(required = false) Long visitorsCntWorldMin,
        @RequestParam(required = false) Long visitorsCntWorldMax
    ) {

        Flux<List<Long>> idss = podWebClient.getErdiIdList(idMask, categoryNames, decisionOrgs, infoTypeIds,
            registryNames, resourceTypes, resourceValue, violationNames, size,
            startTime, endTime, random, sortingDirection, sortingColumn, visitorsCntRussiaMin, visitorsCntRussiaMax,
            visitorsCntWorldMin, visitorsCntWorldMax);

        List<Long> ids = idss.flatMap(Flux::fromIterable).collectList().block();
        if (ids != null) {
            saveErdi(pattern, ids);
        }
        return ids;
    }

    @GetMapping("{id}/custom_erdi")
    public Page<CustomErdiView> findCustomErdi(
        @PathVariable("id") SearchQueryPattern searchQueryPattern,
        @RequestParam(required = false) SortingDirection sortingDirection,
        @RequestParam(required = false) String sortingColumn,
        @RequestParam(defaultValue = "0") int pageNumber,
        @RequestParam(defaultValue = "10") int pageSize){
        PageRequest page = PageRequest.of(
            pageNumber, pageSize, SortingHelper.createSorting(sortingDirection, sortingColumn));
        return customErdiViewRepository.findAllBySearchQueryPatterns(searchQueryPattern, page);
    }

    @PutMapping(path = "/{id}/custom_erdi", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addCustomErdiToPattern(@PathVariable("id") SearchQueryPattern pattern, @RequestBody List<CustomErdi> customErdiList) {
        if(pattern==null){
            throw new AS_15_8_PPT_Exception("Ошибка добавления пользовательских ЕРДИ шаблону! Шаблон не найден в БД");
        }
        if(customErdiList==null){
            throw new AS_15_8_PPT_Exception("Ошибка добавления пользовательских ЕРДИ шаблону! Список пользовательских ЕРДИ пуст");
        }
        log.info("Добавляем список пользовательских ЕРДИ {} в шаблон {}",
            customErdiList.stream().map(customErdi -> customErdi.getId().toString()).collect(Collectors.joining(",")),
            pattern.getId()
            );
        pattern.getCustomErdiList().addAll(customErdiList);
        searchQueryPatternRepo.save(pattern);
    }

    @PutMapping(path = "/{id}/custom_erdi/remove", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void removeCustomErdiFromPattern(@PathVariable("id") SearchQueryPattern pattern, @RequestBody List<CustomErdi> customErdiList) {
        if(pattern==null){
            throw new AS_15_8_PPT_Exception("Ошибка удаления пользовательских ЕРДИ из шаблона! Шаблон не найден в БД");
        }
        if(customErdiList==null){
            throw new AS_15_8_PPT_Exception("Ошибка удаления пользовательских ЕРДИ из шаблона! Список пользовательских ЕРДИ пуст");
        }
        log.info("Удаляем список пользовательских ЕРДИ {} из шаблона {}",
            customErdiList.stream().map(customErdi -> customErdi.getId().toString()).collect(Collectors.joining(",")),
            pattern.getId()
            );
        pattern.getCustomErdiList().removeAll(customErdiList);
        searchQueryPatternRepo.save(pattern);
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

    @PutMapping(path = "/{id}/search_phrases", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void addSearchPhrasesToPattern(@PathVariable("id") SearchQueryPattern pattern, @RequestBody List<SearchPhrase> searchPhrases) {
        if(pattern==null){
            throw new AS_15_8_PPT_Exception("Ошибка добавления поисковых фраз шаблону! Шаблон не найден в БД");
        }
        if(searchPhrases==null){
            throw new AS_15_8_PPT_Exception("Ошибка добавления поисковых фраз шаблону! Список поисковых фраз пуст");
        }
        log.info("Добавляем поисковые фразы {} в шаблон {}",
            searchPhrases.stream().map(SearchPhrase::getPhrase).collect(Collectors.joining(",")),
            pattern.getId()
            );
        pattern.getSearchPhrases().addAll(searchPhrases);
        searchQueryPatternRepo.save(pattern);
    }

    @PutMapping(path = "/{id}/search_phrases/remove", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void removeSearchPhrasesFromPattern(@PathVariable("id") SearchQueryPattern pattern, @RequestBody List<SearchPhrase> searchPhrases) {
        if(pattern==null){
            throw new AS_15_8_PPT_Exception("Ошибка удаления пользовательских ЕРДИ из шаблона! Шаблон не найден в БД");
        }
        if(searchPhrases==null){
            throw new AS_15_8_PPT_Exception("Ошибка удаления поисковых фраз из шаблона! Список поисковых фраз пуст");
        }
        log.info("Удаляем поисковых фраз {} из шаблона {}",
            searchPhrases.stream().map(customErdi -> customErdi.getId().toString()).collect(Collectors.joining(",")),
            pattern.getId()
            );
        pattern.getSearchPhrases().removeAll(searchPhrases);
        searchQueryPatternRepo.save(pattern);
    }

    private void saveErdi(SearchQueryPattern searchQueryPattern, List<Long> ids) {

        List<SearchQueryPatternContentJoin> records = ids.stream()
            .map(id -> new SearchQueryPatternContentJoin(searchQueryPattern, id))
            .collect(Collectors.toList());
        searchQueryPattern.getFormalErdiList().addAll(records);

        searchQueryPatternRepo.save(searchQueryPattern);
    }

    private <T> void update(Set<T> existing, Set<T> changed) {
        existing.clear();
        if (changed != null){
            existing.addAll(changed);
        }
    }
}
