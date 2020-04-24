package webClients;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import rest.ActRequest;

@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ServiceWebClient {

    private static final String CREATE_ACT_URL = "/pod/act";
    private static final String INFO_ABOUT_ACT = "/ppt/arrangements/info_about_act";

    @Value("${gateway.url}")
    private String gatewayUrl;
    private final OAuth2RestTemplate restTemplate;

    public boolean sendActToPOD(@NonNull ActRequest actRequest){
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

    public boolean notifyPPTAboutActInfo(@NonNull ActRequest actRequest){

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        MappingJacksonValue jacksonValue = new MappingJacksonValue(actRequest);
        HttpEntity<MappingJacksonValue> entity = new HttpEntity<>(jacksonValue, headers);

        log.info("Отправка сообщения с обновлением инфо о акте для мероприятия {}, путь: {}", actRequest.getArragementId(), INFO_ABOUT_ACT);
        try {
            restTemplate.put(UriComponentsBuilder
                    .fromHttpUrl(gatewayUrl)
                    .path(INFO_ABOUT_ACT)
                    .build().toString(), entity);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            log.info("Ошибка отправки сообщения с изменением статуса мероприятия, " + actRequest.getArragementId() + " путь: " + INFO_ABOUT_ACT + ", код возврата " + ex.getStatusCode());
            return false;
        }
        return true;
    }

}
