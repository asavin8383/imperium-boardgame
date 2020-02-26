package controllers.ppm;

import checkUnits.CheckUnit;
import checkUnits.CheckUnitType;
import exceptions.AS_15_8_PPT_Exception;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.enums.SearchQueryReplacePattern;
import model.task.Arrangement;
import model.traffic.DynamicTrafficUnit;
import model.traffic.SearchQueryPattern;
import model.traffic.SearchQueryPatternContentJoin;
import model.traffic.Traffic;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import repositories.*;
import webClients.PodWebClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    private final TrafficRepository trafficRepository;
    private final DynamicTrafficUnitRepository dynamicTrafficUnitRepository;

    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<List<CheckUnit>> getAndSendCheckUnits(@RequestParam("id") Long arrangementId) {

        //TODO получать все остальные трафик-юниты тут же
        log.info("Запрос на получение check units мероприятия: " + arrangementId);
        Arrangement arrangement = arrangementRepo.findById(arrangementId).orElseThrow(() ->
                new AS_15_8_PPT_Exception("Arranegemnt не найден по id: " + arrangementId));

        Traffic traffic = trafficRepository.findById(arrangement.getTrafficId()).orElseThrow(() ->
                new AS_15_8_PPT_Exception("Трафик не найден по id: " + arrangement.getTrafficId()));

        List<Long> contentIds = arrangementRepo.listContentIdsByArrangementId(arrangementId);

        contentIds.addAll(getContenIdsForDynamicTraffic(traffic));

        return Flux.concat(
                podWebClient.fetchCheckUnits(contentIds),
                Flux.just(getCustomErdiCheckUnits(arrangementId)),
                Flux.just(getSearchTemplateCheckUnits(arrangementId))
        );
    }

    private List<Long> getContenIdsForDynamicTraffic(Traffic traffic) {
        List<Long> contentIds = new ArrayList<>();
        try {
            Optional<DynamicTrafficUnit> dynamicTrafficUnit = dynamicTrafficUnitRepository.findByTraffic(traffic).stream().findFirst();
            if (dynamicTrafficUnit.isPresent()) {
                log.info("Начат процесс заполнения чек юнитов для динамического трафика");
                Flux<List<Long>> idss = Flux.empty();
                    if (dynamicTrafficUnit.get().getSize() != null) {
                        idss = podWebClient.getErdiIdList(dynamicTrafficUnit.get());
                }
                List<Long> subContent = idss.flatMap(Flux::fromIterable).collectList().block();
                if (subContent != null)
                    contentIds.addAll(subContent);
            }
            return contentIds;
        } catch (Exception ex) {
            throw new AS_15_8_PPT_Exception("Процесс заполнения чек юнитов для динамического трафика неудачен, ошибка: " + ex);
        }
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
