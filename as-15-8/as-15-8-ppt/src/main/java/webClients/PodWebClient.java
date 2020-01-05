package webClients;

import checkUnits.CheckUnit;
import com.fasterxml.jackson.databind.node.ObjectNode;
import enums.SortingDirection;
import exceptions.AS_15_8_PPT_Exception;
import lombok.extern.slf4j.Slf4j;
import model.traffic.CustomErdiView;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PodWebClient {

    private static final String GET_ERDI_URI = "/pod/erdi/single";
    private static final String GET_SUBTYPE_URI = "/pod/subtype/single_string";
    private static final String GET_CHECK_UNITS_URL = "/pod/erdi/checkUnits";
    private static final String PUT_ERDI_WITH_FILTERS = "/pod/erdi/ids";

    private static final int fetchFluxConcurrency = 50;

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
             String sortingColumn

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
                        .queryParam("violationNames,", violationNames == null ? null : String.join(",", violationNames))
                        .queryParam("size", size)
                        .queryParam("startTime", startTime)
                        .queryParam("endTime", endTime)
                        .queryParam("random", random)
                        .queryParam("sortingDirection", sortingDirection)
                        .queryParam("sortingColumn", sortingColumn)
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

    public Flux<List<CheckUnit>> fetchCheckUnits(List<Long> contentIds) {
        List<List<Long>> ids = packListToLists(contentIds, 1000);
        return Flux.fromIterable(ids)
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

    private Flux<CheckUnit> getCheckUnitsByContentId(Long contentId){
        String uri = UriComponentsBuilder
                .fromUriString(GET_CHECK_UNITS_URL)
                .queryParam("id", contentId)
                .build().toString();

        try {
            //log.info("Получение чек-юнитов ЕРДИ {} по запросу: {}", contentId, uri);
            return webClient.get()
                    .uri(uri)
                    .exchange()
                    .flatMapMany(clientResponse -> {
                        if(clientResponse.statusCode().equals(HttpStatus.OK)){
                            log.info("Чек юниты получены успешно, content_id: {}", contentId);
                            return clientResponse.bodyToFlux(CheckUnit.class);
                        } else {
                            log.warn("Ошибка получения чек юнитов по content_id: {}, статус: {}", contentId, clientResponse.statusCode().toString());
                            return Flux.empty();
                        }
                    });
            //return Arrays.asList(oAuth2RestTemplate.getForObject(uri, CheckUnit[].class));
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw AS_15_8_PPT_Exception.logAndGet(log, String.format("Ошибка получения чек-юнитов ЕРДИ %d в ППТ, код возврата %s", contentId, ex.getStatusCode()), ex);
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

    private static <T>List<List<T>> packListToLists(List<T> list, int subListSize) {
        final AtomicInteger counter = new AtomicInteger();
        return new ArrayList<>(list.stream()
                .collect(Collectors.groupingBy(it -> counter.getAndIncrement() / subListSize))
                .values());
    }
}
