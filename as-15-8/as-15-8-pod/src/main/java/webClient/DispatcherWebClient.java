package webClient;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import rest.ActCheckResult;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
@Slf4j
public class DispatcherWebClient {

    @Value("${gateway.url}")
    private String gatewayUrl;

    @Value("${act.screenshotes.max-count}")
    private Long maxCountActScreenShots;

    private static final String ACT_CHECK_RESULTS_URI = "/dispatcher/act/checkResult";
    private static final String ACT_SCREENSHOTES_URI = "/dispatcher/act/screenshots";

    private WebClient webClient;

    @PostConstruct
    private void init(){
        webClient = WebClient.create(gatewayUrl);
    }

    public List<ActCheckResult> getActCheckResults(Long arrangementId){
        return webClient
                .get()
                .uri(UriComponentsBuilder
                        .fromUriString(ACT_CHECK_RESULTS_URI)
                        .queryParam("arrangementId", arrangementId)
                        .build().toString()
                )
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(ActCheckResult.class)
                .collectList()
                .block();
    }

    public List<String> getActScreenshotesBase64(Long arrangementId){
        UriComponentsBuilder uriBuilder = UriComponentsBuilder
                .fromUriString(ACT_SCREENSHOTES_URI)
                .queryParam("arrangementId", arrangementId);

        if(maxCountActScreenShots != null)
            uriBuilder.queryParam("maxCountScreenShots", maxCountActScreenShots);

        return webClient
                .get()
                .uri(uriBuilder.build().toString())
                .accept(MediaType.TEXT_EVENT_STREAM)
                .retrieve()
                .bodyToFlux(String.class)
                .collectList()
                .block();
    }

}
