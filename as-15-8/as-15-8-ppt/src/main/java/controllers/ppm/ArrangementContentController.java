package controllers.ppm;

import checkUnits.CheckUnit;
import checkUnits.CheckUnitType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.enums.SearchQueryReplacePattern;
import model.traffic.SearchQueryPattern;
import model.traffic.SearchQueryPatternContentJoin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import repositories.ArrangementRepo;
import repositories.CustomErdiUnitRepository;
import repositories.SearchQueryPatternRepo;
import webClients.PodWebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by san
 * Date: 03.11.2019
 */
@Slf4j
@RestController
@RequestMapping(path = "arrangements/checkUnits", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class ArrangementContentController {

    private final ArrangementRepo arrangementRepo;
    private final CustomErdiUnitRepository customErdiUnitRepository;
    private final PodWebClient podWebClient;
    private final SearchQueryPatternRepo searchQueryPatternRepo;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<List<CheckUnit>> getAndSendCheckUnits(@RequestParam("id") Long arrangementId) {

        //TODO получать все остальные трафик-юниты тут же
        log.info("Запрос на получение check units мероприятия: " + arrangementId);
        List<Long> contentIds = arrangementRepo.listContentIdsByArrangementId(arrangementId);
        return Flux.concat(
                podWebClient.fetchCheckUnits(contentIds),
                Flux.just(getCustomErdiCheckUnits(arrangementId)),
                Flux.just(getSearchTemplateCheckUnits(arrangementId))
        );
    }

    private List<CheckUnit> getCustomErdiCheckUnits(Long arrangementId){
        return customErdiUnitRepository
                .findByArrangementId(arrangementId)
                .stream()
                .map(customErdiUnit -> new CheckUnit(null, customErdiUnit.getType(), customErdiUnit.getValue()))
                .collect(Collectors.toList());
    }

    private List<CheckUnit> getSearchTemplateCheckUnits(Long arrangementId){
        List<CheckUnit> checkUnits = new ArrayList<>();
        List<SearchQueryPattern> searchQueryPatterns =
                searchQueryPatternRepo
                .findAllByArrangement(arrangementId);
        searchQueryPatterns.forEach(searchQueryPattern -> {
            log.info("Добавляем поисковые шаблоны в мероприятие {} для заданного шаблона {}", arrangementId, searchQueryPattern.getQueryPattern());
            List<CheckUnit> erdiCheckUnits = fillErdi(searchQueryPattern);
            List<CheckUnitJoinSearchPhrase> checkUnitJoinSearchPhrases = new ArrayList<>();
            //Собираем суррогатную коллекцию из ЕРДИ и фраз кросс-джойном
            if (erdiCheckUnits.size() > 0){
                log.info("Заполняем шаблон записями ЕРДИ для мероприятия {}", arrangementId);
                //если список поисковых фраз непустой, делаем кросс-джойн
                if (searchQueryPattern.getSearchPhrases().size() > 0) {
                    checkUnitJoinSearchPhrases.addAll(
                            erdiCheckUnits.stream()
                                    .flatMap(checkUnit ->
                                            searchQueryPattern.getSearchPhrases().stream()
                                                    .map(searchPhrase -> new CheckUnitJoinSearchPhrase(checkUnit, searchPhrase.getPhrase())))
                                    .collect(Collectors.toList())
                    );
                } else {
                    log.info("Заполняем шаблон поисковыми фразами для мероприятия {}", arrangementId);
                    //Если в шаблоне нет фраз, то в чек-юниты войдут только чек-юниты
                    checkUnitJoinSearchPhrases.addAll(erdiCheckUnits
                            .stream()
                            .map(checkUnit -> new CheckUnitJoinSearchPhrase(checkUnit, null))
                            .collect(Collectors.toList())
                    );
                }
            } else {
                log.info("Заполняем шаблон поисковыми фразами для мероприятия {}", arrangementId);
                //Если в шаблоне нет ЕРДИ, то в чек-юниты войдут только фразы
                checkUnitJoinSearchPhrases.addAll(searchQueryPattern.getSearchPhrases()
                        .stream()
                        .map(searchPhrase -> new CheckUnitJoinSearchPhrase(null, searchPhrase.getPhrase()))
                        .collect(Collectors.toList())
                );
            }
            //Если список собрался, будем генерить чек-юниты, иначе в чек-юнит войдёт только поисковая фраза
            if (checkUnitJoinSearchPhrases.size() > 0) {
                log.info("Выполняем генерацию чек-юнитов по шаблону для мероприятия {}", arrangementId);
                checkUnitJoinSearchPhrases
                    .forEach(checkUnitJoinSearchPhrase ->
                        checkUnits
                            .add(createFromJoin(searchQueryPattern.getQueryPattern(), checkUnitJoinSearchPhrase)));
            } else {
                checkUnits.add(new CheckUnit(null, CheckUnitType.SEARCH_PHRASE, searchQueryPattern.getQueryPattern()));
            }

        });

        return checkUnits;
    }

    private List<CheckUnit> fillErdi(SearchQueryPattern searchQueryPattern) {
        List<CheckUnit> checkUnits = new ArrayList<>();
        String patternString = searchQueryPattern.getQueryPattern();
        if(patternString != null && patternString.contains(SearchQueryReplacePattern.ERDI.getPattern())){
            List<Long> contentIds = searchQueryPattern.getFormalErdiList()
                .stream()
                .mapToLong(SearchQueryPatternContentJoin::getContentId)
                .boxed()
                .collect(Collectors.toList());
            List<CheckUnit> podCheckUnits = podWebClient
                    .fetchCheckUnits(contentIds)
                    .flatMap(checkUnitList -> Flux.fromIterable(Objects.requireNonNull(checkUnitList)))
                    .collectList()
                    .block();
            if(podCheckUnits != null) {
                checkUnits.addAll(podCheckUnits);
            }
            searchQueryPattern.getCustomErdiList().forEach(
                    customErdi -> customErdi.getCustomErdiUnits().forEach(
                            customErdiUnit -> checkUnits.add(
                                    new CheckUnit(null, customErdiUnit.getType(), customErdiUnit.getValue()))
                    )
            );
        }
        return checkUnits;
    }

    private CheckUnit createFromJoin(String pattern, CheckUnitJoinSearchPhrase join){
        return new CheckUnit(
            null,
            CheckUnitType.SEARCH_PHRASE,
            pattern
                .replace(SearchQueryReplacePattern.ERDI.getPattern(), join.getCheckUnit() == null ? "" : join.getCheckUnit().getValue())
                .replace(SearchQueryReplacePattern.EXPRESSION.getPattern(), join.getSearchPhrase() == null ? "" : join.getSearchPhrase())
        );
    }

    @Data
    @AllArgsConstructor
    private static class CheckUnitJoinSearchPhrase {
        private CheckUnit checkUnit;
        private String searchPhrase;
    }
}
