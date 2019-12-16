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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Created by san
 * Date: 03.11.2019
 */
@Slf4j
@RestController
@RequestMapping(path = "arrangements/checkUnits", produces = MediaType.APPLICATION_JSON_VALUE)
//@PreAuthorize("hasRole('ROLE_SYSTEM')")
@RequiredArgsConstructor(onConstructor_={@Autowired})
public class ArrangementContentController {

    private final ArrangementRepo arrangementRepo;
    private final CustomErdiUnitRepository customErdiUnitRepository;
    private final PodWebClient podWebClient;
    private final SearchQueryPatternRepo searchQueryPatternRepo;

    /*@GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<CheckUnit> getAndSendCheckUnits(@RequestParam("id") Long arrangementId) {

        //TODO получать все остальные трафик-юниты тут же
        log.info("Запрос на получение check units мероприятия: " + arrangementId);
        List<Long> contentIds = arrangementRepo.listContentIdsByArrangementId(arrangementId);
        Flux<CheckUnit> checkUnits = Flux.concat(
                podWebClient.fetchCheckUnits(contentIds),
                getCustomErdiCheckUnits(arrangementId),
                getSearchTemplateCheckUnits(arrangementId)
        );
        log.info("Сформирован список check units мероприятия: " + arrangementId);

        return checkUnits;
    }*/

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<List<CheckUnit>> getAndSendCheckUnits(@RequestParam("id") Long arrangementId) {

        //TODO получать все остальные трафик-юниты тут же
        log.info("Запрос на получение check units мероприятия: " + arrangementId);
        List<Long> contentIds = arrangementRepo.listContentIdsByArrangementId(arrangementId);
        Flux<CheckUnit> checkUnits = Flux.concat(
                podWebClient.fetchCheckUnits(contentIds),
                getCustomErdiCheckUnits(arrangementId),
                getSearchTemplateCheckUnits(arrangementId)
        );
        log.info("Сформирован список check units мероприятия: " + arrangementId);

        List<CheckUnit> list = checkUnits.toStream().collect(Collectors.toList());
        List<List<CheckUnit>> lists = packCheckUnitListToList(list);

        Flux<List<CheckUnit>> result = Flux.fromIterable(lists);

        return result;
    }

    private static List<List<CheckUnit>> packCheckUnitListToList(List<CheckUnit> checkUnitList) {
        // результат работы метода сделан для flux и hystrix
        final AtomicInteger counter = new AtomicInteger();
        int subListSize = 1000;

        List<List<CheckUnit>> result = new ArrayList<>(checkUnitList.stream()
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / subListSize))
                .values());
        return result;
    }

    private Flux<CheckUnit> getCustomErdiCheckUnits(Long arrangementId){
        return Flux.fromIterable(customErdiUnitRepository
                .findByArrangementId(arrangementId)
                .stream()
                .map(customErdiUnit -> new CheckUnit(null, customErdiUnit.getType(), customErdiUnit.getValue()))
                .collect(Collectors.toList()));
    }

    private Flux<CheckUnit> getSearchTemplateCheckUnits(Long arrangementId){
        List<CheckUnit> checkUnits = new ArrayList<>();
        List<SearchQueryPattern> searchQueryPatterns =
                searchQueryPatternRepo
                .findAllByArrangement(arrangementId);
        searchQueryPatterns.forEach(searchQueryPattern -> {
            List<CheckUnit> erdiCheckUnits = fillErdi(searchQueryPattern);
            List<CheckUnitJoinSearchPhrase> checkUnitJoinSearchPhrases = new ArrayList<>();
            //Собираем суррогатную коллекцию из ЕРДИ и фраз кросс-джойном
            if (erdiCheckUnits.size() > 0){
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
                    //Если в шаблоне нет фраз, то в чек-юниты войдут только чек-юниты
                    checkUnitJoinSearchPhrases.addAll(erdiCheckUnits
                            .stream()
                            .map(checkUnit -> new CheckUnitJoinSearchPhrase(checkUnit, null))
                            .collect(Collectors.toList())
                    );
                }
            } else {
                //Если в шаблоне нет ЕРДИ, то в чек-юниты войдут только фразы
                checkUnitJoinSearchPhrases.addAll(searchQueryPattern.getSearchPhrases()
                        .stream()
                        .map(searchPhrase -> new CheckUnitJoinSearchPhrase(null, searchPhrase.getPhrase()))
                        .collect(Collectors.toList())
                );
            }
            //Если список собрался, будем генерить чек-юниты, иначе в чек-юнит войдёт только поисковая фраза
            if (checkUnitJoinSearchPhrases.size() > 0) {
                checkUnitJoinSearchPhrases
                    .forEach(checkUnitJoinSearchPhrase ->
                        checkUnits
                            .add(createFromJoin(searchQueryPattern.getQueryPattern(), checkUnitJoinSearchPhrase)));
            } else {
                checkUnits.add(new CheckUnit(null, CheckUnitType.SEARCH_PHRASE, searchQueryPattern.getQueryPattern()));
            }

        });

        return Flux.fromIterable(checkUnits);
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
            List<CheckUnit> podCheckUnits = podWebClient.fetchCheckUnits(contentIds).sequential().collectList().block();
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
