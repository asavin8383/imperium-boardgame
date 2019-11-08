package webClients;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class POD_WebClient {

    private static final String GET_ERDI_URI = "/pod/erdi/single";

    @Value("${gateway.url}")
    private String gatewayUrl;

    private WebClient webClient;

    @PostConstruct
    private void initWebClient() {
        webClient = WebClient.create(gatewayUrl);
    }

    public List<ObjectNode> fetchErdi(List<Long> erdiIds) {
        return Flux.fromIterable(erdiIds)
                .parallel()
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
                .bodyToMono(ObjectNode.class);
    }

}
