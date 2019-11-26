package controllers.ppm;

import checkUnits.CheckUnit;
import checkUnits.CheckUnitType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.enums.SearchQueryPattern;
import model.traffic.SearchQueryContentJoin;
import model.traffic.SearchQueryTrafficUnit;
import org.reactivestreams.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.ParallelFlux;
import repositories.ArrangementRepo;
import repositories.CustomErdiUnitRepository;
import repositories.SearchQueryTrafficUnitRepository;
import webClients.PodWebClient;

import java.util.ArrayList;
import java.util.List;
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
    private final SearchQueryTrafficUnitRepository searchQueryTrafficUnitRepository;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ParallelFlux<CheckUnit> getAndSendCheckUnits(@RequestParam("id") Long arrangementId) {

        //TODO получать все остальные трафик-юниты тут же
        List<Long> contentIds = arrangementRepo.listContentIdsByArrangementId(arrangementId);
       // ParallelFlux<CheckUnit> checkUnitFlux = pod_webClient.fetchCheckUnits(contentIds);
       /* return Flux
                .concat(checkUnitFlux,
                        Flux.fromIterable(getCustomErdiCheckUnits(arrangementId)),
                        Flux.fromIterable(getSearchPhrasesCheckUnits(arrangementId))
                );*/
       return podWebClient.fetchCheckUnits(contentIds);
    }

    private List<CheckUnit> getCustomErdiCheckUnits(Long arrangementId){
        return customErdiUnitRepository
                .findByArrangementId(arrangementId)
                .stream()
                .map(customErdiUnit -> new CheckUnit(null, customErdiUnit.getType(), customErdiUnit.getValue()))
                .collect(Collectors.toList());
    }

    private List<CheckUnit> getSearchPhrasesCheckUnits(Long arrangementId){
        List<CheckUnit> checkUnits = new ArrayList<>();
        List<SearchQueryTrafficUnit> searchQueryTrafficUnits =
                searchQueryTrafficUnitRepository
                .findByArrangement(arrangementId);
        searchQueryTrafficUnits.forEach(searchQueryTrafficUnit ->  {
            List<CheckUnit> erdiCheckUnits = fillErdi(searchQueryTrafficUnit);
            List<CheckUnitJoinSearchPhrase> checkUnitJoinSearchPhrases = new ArrayList<>();
            //Собираем суррогатную коллекцию из ЕРДИ и фраз кросс-джойном
            if (erdiCheckUnits.size() > 0){
                //если список поисковых фраз непустой, делаем кросс-джойн
                if (searchQueryTrafficUnit.getSearchPhrases().size() > 0) {
                    checkUnitJoinSearchPhrases.addAll(
                            erdiCheckUnits.stream()
                                    .flatMap(checkUnit ->
                                            searchQueryTrafficUnit.getSearchPhrases().stream()
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
                checkUnitJoinSearchPhrases.addAll(searchQueryTrafficUnit.getSearchPhrases()
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
                            .add(createFromJoin(searchQueryTrafficUnit.getQueryPattern(), checkUnitJoinSearchPhrase)));
            } else {
                checkUnits.add(new CheckUnit(null, CheckUnitType.SEARCH_PHRASE, searchQueryTrafficUnit.getQueryPattern()));
            }

        });

        return checkUnits;
    }

    private List<CheckUnit> fillErdi(SearchQueryTrafficUnit searchQueryTrafficUnit) {
        List<CheckUnit> checkUnits = new ArrayList<>();
        String searchQueryPattern = searchQueryTrafficUnit.getQueryPattern();
        if(searchQueryPattern != null && searchQueryPattern.contains(SearchQueryPattern.ERDI.getPattern())){
            List<Long> contentIds = searchQueryTrafficUnit.getFormalErdiList()
                .stream()
                .mapToLong(SearchQueryContentJoin::getContentId)
                .boxed()
                .collect(Collectors.toList());
            List<CheckUnit> podCheckUnits = podWebClient.fetchCheckUnits(contentIds).sequential().collectList().block();
            if(podCheckUnits != null) {
                checkUnits.addAll(podCheckUnits);
            }
            searchQueryTrafficUnit.getCustomErdiList().forEach(
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
                .replace(SearchQueryPattern.ERDI.getPattern(), join.getCheckUnit() == null ? "" : join.getCheckUnit().getValue())
                .replace(SearchQueryPattern.EXPRESSION.getPattern(), join.getSearchPhrase() == null ? "" : join.getSearchPhrase())
        );
    }

    @Data
    @AllArgsConstructor
    private static class CheckUnitJoinSearchPhrase {
        private CheckUnit checkUnit;
        private String searchPhrase;
    }
}
