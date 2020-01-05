package webClient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import rest.ActAttachment;
import rest.ActCheckResult;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@Slf4j
public class DispatcherWebClient {

    @Value("${gateway.url}")
    private String gatewayUrl;

    private static final String ACT_CHECK_RESULTS_URI = "/dispatcher/act/checkResult";
    private static final String ACT_SCREENSHOTES_URI = "/dispatcher/act/screenshots";

    private WebClient webClient;

    @PostConstruct
    private void init(){
        webClient = WebClient.create(gatewayUrl);
    }

    public Flux<List<ActCheckResult>> getActCheckResults(Long arrangementId){
        return webClient
                .get()
                .uri(UriComponentsBuilder
                        .fromUriString(ACT_CHECK_RESULTS_URI)
                        .queryParam("arrangementId", arrangementId)
                        .build().toString()
                )
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .flatMapMany(clientResponse -> {
                    if(clientResponse.statusCode().equals(HttpStatus.OK)){
                        log.info("Считывание actCheckResult успешно");
                        return clientResponse.bodyToFlux(new ParameterizedTypeReference<List<ActCheckResult>>(){});
                    } else {
                        log.warn("Считывание actCheckResult не успешно, статус: {}", clientResponse.statusCode().toString());
                        return Flux.empty();
                    }
                });
    }

    public Flux<List<ActAttachment>> getActAttachments(Long arrangementId){
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(ACT_SCREENSHOTES_URI)
                .queryParam("arrangementId", arrangementId);

        return webClient
                .get()
                .uri(uriBuilder.build().toString())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .flatMapMany(clientResponse -> {
                    if(clientResponse.statusCode().equals(HttpStatus.OK)){
                        log.info("Считывание скриншотов прошло успешно");
                        return clientResponse.bodyToFlux(new ParameterizedTypeReference<List<ActAttachment>>(){});
                    } else {
                        log.warn("Считывание скриншотов не успешно, статус: {}", clientResponse.statusCode().toString());
                        return Flux.empty();
                    }
                });
    }
}
