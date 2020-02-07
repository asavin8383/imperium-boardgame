package controllers;

import enums.Dictionary;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.traffic.DictionaryView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import repositories.CustomErdiRepository;
import repositories.SearchPhraseRepository;
import repositories.SearchQueryPatternRepo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by san
 * Date: 21.11.2019
 */
@RestController
@Slf4j
@PreAuthorize("hasRole('ROLE_DICTIONARY')")
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@RequestMapping(path = "/dict", produces = MediaType.APPLICATION_JSON_VALUE)
public class DictionaryController {

    private final CustomErdiRepository customErdiRepository;
    private final SearchPhraseRepository searchPhraseRepository;
    private final SearchQueryPatternRepo searchQueryPatternRepo;

    @GetMapping
    public List<DictionaryView> getDictionaryInfo(){
        List<DictionaryView> dictionaryViews = new ArrayList<>();
        dictionaryViews.add(
                new DictionaryView(
                        Dictionary.USER_ERDI.toString(),
                        customErdiRepository.count(),
                        Dictionary.USER_ERDI.getShortName())
        );
        dictionaryViews.add(
                new DictionaryView(
                        Dictionary.SEARCH_PHRASES.toString(),
                        searchPhraseRepository.count(),
                        Dictionary.SEARCH_PHRASES.getShortName())
        );
        dictionaryViews.add(
                new DictionaryView(
                        Dictionary.SEARCH_QUERY_PATTERNS.toString(),
                        searchQueryPatternRepo.count(),
                        Dictionary.SEARCH_QUERY_PATTERNS.getShortName())
        );
        return dictionaryViews;

    }
}
