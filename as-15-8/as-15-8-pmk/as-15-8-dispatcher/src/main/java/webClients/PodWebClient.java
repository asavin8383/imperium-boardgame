package webClients;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import rest.ActRequest;

@Service
@Slf4j
public class PodWebClient {

    private static final String CREATE_ACT_URL = "/pod/act";

    @Value("${gateway.url}")
    private String gatewayUrl;

    public boolean sendAct(@NonNull ActRequest actRequest){
        try {
            ClientResponse resp = WebClient.create(gatewayUrl)
                    .post()
                    .uri(UriComponentsBuilder
                            .fromUriString(CREATE_ACT_URL)
                            .build().toString())
                    .body(BodyInserters.fromObject(actRequest))
                    .exchange()
                    .block();
            assert resp != null;
            log.info("Статус отправки акта в ПОД: {}. Акт: {}", resp.statusCode().toString(), actRequest);
            return resp.statusCode().is2xxSuccessful();
        } catch(Exception ex){
            log.error("Ошибка отправки запроса на создание акта по мероприятию " + actRequest.getArragementId(), ex);
            return false;
        }
    }

}
