package webClients;

import checkUnits.CheckUnit;
import com.fasterxml.jackson.databind.node.ObjectNode;
import exceptions.AS_15_8_PPT_Exception;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.ParallelFlux;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@Slf4j
public class PodWebClient {

    private static final String GET_ERDI_URI = "/pod/erdi/single";
    private static final String GET_CHECK_UNITS_URL = "/pod/erdi/checkUnits";

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
                .retrieve()
                .bodyToMono(ObjectNode.class)
                .doOnError(ex -> log.error("Ошибка при получении ЕРДИ по id: "+id, ex));
    }

    public ParallelFlux<CheckUnit> fetchCheckUnits(List<Long> contentIds) {
        return Flux.fromIterable(contentIds)
                .parallel(fetchFluxConcurrency)
                .runOn(Schedulers.parallel())
                .flatMap(this::getCheckUnitsByContentId);
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

}
