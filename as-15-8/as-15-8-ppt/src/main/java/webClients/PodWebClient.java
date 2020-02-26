package webClients;

import checkUnits.CheckUnit;
import com.fasterxml.jackson.databind.node.ObjectNode;
import enums.SortingDirection;
import exceptions.AS_15_8_PPT_Exception;
import lombok.extern.slf4j.Slf4j;
import model.enums.TrafficType;
import model.traffic.CustomErdiView;
import model.traffic.DynamicTrafficUnit;
import model.traffic.Traffic;
import model.traffic.TrafficBriefView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
public class PodWebClient {

    private static final String GET_ERDI_URI = "/pod/erdi/single";
    private static final String GET_SUBTYPE_URI = "/pod/subtype/single_string";
    private static final String GET_CHECK_UNITS_URL = "/pod/erdi/checkUnits";
    private static final String PUT_ERDI_WITH_FILTERS = "/pod/erdi/ids";
    private static final String GET_ERDI_CHECK_UNITS_COUNT = "pod/erdi/check_units_count";

    private static final int fetchFluxConcurrency = 50;
    private static final int BUFFER_SIZE = 1000;

    @Value("${gateway.url}")
    private String gatewayUrl;

    private WebClient webClient;

    @PostConstruct
    private void initWebClient() {
        webClient = WebClient.create(gatewayUrl);
    }

    public List<ObjectNode> fetchErdi(List<Long> erdiIds) {
        return Flux.fromIterable(erdiIds)
                .parallel(fetchFluxConcurrency)
                .runOn(Schedulers.parallel())
                .flatMap(this::getErdi)
                .sequential()
                .collectList()
                .block();
    }

    private Mono<ObjectNode> getErdi(Long id) {
        return webClient.get()
                .uri(UriComponentsBuilder
                        .fromUriString(GET_ERDI_URI)
                        .queryParam("id", id)
                        .build().toString())
                .exchange()
                .flatMap(clientResponse -> {
                    if(clientResponse.statusCode().equals(HttpStatus.OK)){
                        log.info("ЕРДИ получен успешно, id: {}", id);
                        return clientResponse.bodyToMono(ObjectNode.class);
                    } else {
                        log.warn("Ошибка при получении ЕРДИ по id {}, статус: {}", id, clientResponse.statusCode().toString());
                        return Mono.empty();
                    }
                });
    }

    public ResponseEntity getTrafficUnitContentIdsFiltered(
           Pageable pageable,
           List<Long> contentIds

    ) {
        Sort.Order order = pageable.getSort().stream().findFirst().orElseThrow(() ->
                new AS_15_8_PPT_Exception("Невозможно отправить запрос на получение сортированных записей ЕРДИ в ПОД, т.к сортировка не задана"));

        return webClient.post()
                .uri(UriComponentsBuilder
                        .fromUriString(PUT_ERDI_WITH_FILTERS)
                        .queryParam("sortingDirection", SortingDirection.valueOf(order.getDirection().name()))
                        .queryParam("sortingColumn", order.getProperty())
                        .queryParam("pageNumber", pageable.getPageNumber())
                        .queryParam("pageSize", pageable.getPageSize())
                        .build().toString())
                .body(BodyInserters.fromObject(contentIds))
                .exchange()
                .flatMap(clientResponse -> {
                    if (clientResponse.statusCode().equals(HttpStatus.OK)) {
                        log.info("список id ЕРДИ считан успешно");

                        return clientResponse
                                .bodyToMono(ObjectNode.class)
                                .flatMap(objectNodes -> Mono.just(ResponseEntity.ok(objectNodes)));

                    } else {
                        log.warn("Ошибка при чтении списка id ЕРДИ, статус: {}", clientResponse.statusCode().toString());
                        return clientResponse
                                .bodyToMono(String.class)
                                .flatMap(error -> Mono.just(ResponseEntity.badRequest().body(error)));

                    }
                }).block();

    }

    public Flux<List<Long>> getErdiIdList(
             String idMask,
             List<String> categoryNames,
             List<String> decisionOrgs,
             List<String> infoTypeIds,
             List<String> registryNames,
             List<String> resourceTypes,
             String resourceValue,
             List<String> violationNames,
             Integer size,
             LocalDate startTime,
             LocalDate endTime,
             Boolean random,
             SortingDirection sortingDirection,
             String sortingColumn,
             Long visitorsCntRussiaMin,
             Long visitorsCntRussiaMax,
             Long visitorsCntWorldMin,
             Long visitorsCntWorldMax

    ) {
        return webClient.get()
                .uri(UriComponentsBuilder
                        .fromUriString(PUT_ERDI_WITH_FILTERS)
                        .queryParam("idMask", idMask)
                        .queryParam("categoryNames", categoryNames == null ? null : String.join(",", categoryNames))
                        .queryParam("decisionOrgs", decisionOrgs == null ? null : String.join(",", decisionOrgs))
                        .queryParam("infoTypeIds", infoTypeIds == null ? null : String.join(",", infoTypeIds))
                        .queryParam("registryNames", registryNames == null ? null : String.join(",", registryNames))
                        .queryParam("resourceTypes", resourceTypes == null ? null : String.join(",", resourceTypes))
                        .queryParam("resourceValue", resourceValue)
                        .queryParam("violationNames", violationNames == null ? null : String.join(",", violationNames))

                        .queryParam("size", size)
                        .queryParam("startTime", startTime)
                        .queryParam("endTime", endTime)
                        .queryParam("random", random)
                        .queryParam("sortingDirection", sortingDirection)
                        .queryParam("sortingColumn", sortingColumn)
                        .queryParam("visitorsCntRussiaMin", visitorsCntRussiaMin)
                        .queryParam("visitorsCntRussiaMax", visitorsCntRussiaMax)
                        .queryParam("visitorsCntWorldMin", visitorsCntWorldMin)
                        .queryParam("visitorsCntWorldMax", visitorsCntWorldMax)
                        .build().toString())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .flatMapMany(clientResponse -> {
                    if(clientResponse.statusCode().equals(HttpStatus.OK)){
                        log.info("список id ЕРДИ считан успешно");
                        return clientResponse.bodyToFlux(new ParameterizedTypeReference<List<Long>>(){});
                    } else {
                        log.warn("Ошибка при чтении списка id ЕРДИ, статус: {}", clientResponse.statusCode().toString());
                        return Flux.empty();
                    }
                });
    }
    //TODO убрать, как только будет готов новый фронт под трафик
    public Flux<List<Long>> getErdiIdList(String query) {
        return webClient.get()
                .uri(UriComponentsBuilder
                        .fromUriString(PUT_ERDI_WITH_FILTERS)
                        .queryParam("query", query)
                        .build().toString())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .flatMapMany(clientResponse -> {
                    if(clientResponse.statusCode().equals(HttpStatus.OK)){
                        log.info("список id ЕРДИ считан успешно");
                        return clientResponse.bodyToFlux(new ParameterizedTypeReference<List<Long>>(){});
                    } else {
                        log.warn("Ошибка при чтении списка id ЕРДИ, статус: {}", clientResponse.statusCode().toString());
                        return Flux.empty();
                    }
                });
    }

    public Flux<List<Long>> getErdiIdList(DynamicTrafficUnit dynamicTrafficUnit) {
        return getErdiIdList(dynamicTrafficUnit.getIdMask(),
                            dynamicTrafficUnit.getCategoryNames(),
                            dynamicTrafficUnit.getDecisionOrgs(),
                            dynamicTrafficUnit.getInfoTypeIds(),
                            dynamicTrafficUnit.getRegistryNames(),
                            dynamicTrafficUnit.getResourceTypes(),
                            dynamicTrafficUnit.getResourceValue(),
                            dynamicTrafficUnit.getViolationNames(),
                            dynamicTrafficUnit.getSize(),
                            dynamicTrafficUnit.getStartTime(),
                            dynamicTrafficUnit.getEndTime(),
                            dynamicTrafficUnit.getRandom(),
                            dynamicTrafficUnit.getSortingDirection(),
                            dynamicTrafficUnit.getSortingColumn(),
                            dynamicTrafficUnit.getVisitorsCntRussiaMin(),
                            dynamicTrafficUnit.getVisitorsCntRussiaMax(),
                            dynamicTrafficUnit.getVisitorsCntWorldMin(),
                            dynamicTrafficUnit.getVisitorsCntWorldMax());
    }

    public Flux<List<CheckUnit>> fetchCheckUnits(List<Long> contentIds) {
        return Flux.fromIterable(contentIds)
                .buffer(BUFFER_SIZE)
                .map(this::getCheckUnitsByContentIds);
    }

    private List<CheckUnit> getCheckUnitsByContentIds(List<Long> contentIds){
        String uri = UriComponentsBuilder
                .fromUriString(GET_CHECK_UNITS_URL)
                .build().toString();

        try {
            return webClient
                    .post()
                    .uri(uri)
                    .body(BodyInserters.fromObject(contentIds))
                    .exchange()
                    .flatMapMany(clientResponse -> {
                        if(clientResponse.statusCode().equals(HttpStatus.OK)){
                            log.info("Чек юниты получены успешно");
                            return clientResponse.bodyToFlux(CheckUnit.class);
                        } else {
                            log.warn("Ошибка получения чек юнитов, статус: {}", clientResponse.statusCode().toString());
                            return Flux.empty();
                        }
                    })
                    .collectList()
                    .block();
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw AS_15_8_PPT_Exception.logAndGet(log, String.format("Ошибка получения чек-юнитов в ППТ, код возврата %s", ex.getStatusCode()), ex);
        }
    }

    public List<CustomErdiView> fetchSubtypes(List<CustomErdiView> customErdiViewList) {
        return Flux.fromIterable(customErdiViewList)
            .parallel(fetchFluxConcurrency)
            .runOn(Schedulers.parallel())
            .flatMap(customErdiView ->
                this.getSubtype(customErdiView.getSubtypeId())
                    .map(subtype -> {
                        customErdiView.setSubtype(subtype);
                        return customErdiView;
                    })
            )
            .sequential()
            .collectList()
            .block();
    }

    private Mono<String> getSubtype(String origId) {
        return webClient.get()
            .uri(UriComponentsBuilder
                .fromUriString(GET_SUBTYPE_URI)
                .queryParam("origId", origId)
                .build().toString())
            .exchange()
            .flatMap(clientResponse -> {
                if(clientResponse.statusCode().equals(HttpStatus.OK)){
                    log.info("Subtype получен успешно, id: {}", origId);
                    return clientResponse.bodyToMono(String.class);
                } else {
                    log.warn("Ошибка при получении Subtype по id {}, статус: {}", origId, clientResponse.statusCode().toString());
                    return Mono.empty();
                }
            });
    }

    private Mono<Long> getErdiIdsCheckUnitCount(List<Long> erdiIds) {
        if(erdiIds == null || erdiIds.size()==0)
            return Mono.just(0L);
        return webClient.post()
            .uri(createUri(GET_ERDI_CHECK_UNITS_COUNT))
            .body(BodyInserters.fromObject(erdiIds))
            .exchange()
            .flatMap(clientResponse -> {
                if(clientResponse.statusCode().equals(HttpStatus.OK)){
                    return clientResponse.bodyToMono(Long.class);
                } else {
                    return Mono.just(0L);
                }
            });
    }

    private String createUri(String endPoint) {
        return UriComponentsBuilder.fromUriString(endPoint).build().toString();
    }

    public Mono<TrafficBriefView> fetchActualCheckUnitCount(Traffic traffic, List<Long> erdiIds) {
        if(erdiIds == null || erdiIds.size() == 0)
            return Mono.just(createTrafficBriefView(traffic, 0L));
        return Flux.fromIterable(erdiIds)
                .buffer(5000)
                .parallel(fetchFluxConcurrency)
                .runOn(Schedulers.parallel())
                .flatMap(this::getErdiIdsCheckUnitCount)
                .map(staticCount -> createTrafficBriefView(traffic, staticCount))
                .reduce((acc, current) -> {
                    acc.setActualCheckUnitsCount(acc.getActualCheckUnitsCount()+current.getActualCheckUnitsCount());
                    //acc.setCount(acc.getCount()+current.getCount());
                    return acc;
                });
    }

    public Long calculateActualCheckUnitCount(List<Long> erdiIds) {
        if(erdiIds == null || erdiIds.size() == 0)
            return 0L;

        return Flux.fromIterable(erdiIds)
                .buffer(5000)
                .parallel(fetchFluxConcurrency)
                .runOn(Schedulers.parallel())
                .flatMap(this::getErdiIdsCheckUnitCount)
                .reduce(Long::sum)
                .block();
    }

    private TrafficBriefView createTrafficBriefView(Traffic traffic, Long staticCount) {
        TrafficBriefView view = new TrafficBriefView(traffic.getId(), traffic.getName());
        long dynamicCount = 0;
        //view.setCount(staticCount + dynamicCount);
        view.setActualCheckUnitsCount(staticCount + dynamicCount);
        view.setType(getTrafficType(staticCount, dynamicCount));
        return view;
    }

    private TrafficType getTrafficType(long staticCount, long dynamicCount) {
        if (staticCount == 0 && dynamicCount > 0)
            return TrafficType.DYNAMIC;
        else if (staticCount > 0 && dynamicCount == 0)
            return TrafficType.STATIC;
        else
            return TrafficType.MIXED;
    }
}
