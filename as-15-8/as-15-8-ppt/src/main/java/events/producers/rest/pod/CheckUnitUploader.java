package events.producers.rest.pod;

import checkUnits.CheckUnit;
import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by san
 * Date: 02.11.2019
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class CheckUnitUploader {

    @Value("${gateway.url}")
    private String gatewayUrl;

    private final OAuth2RestTemplate oAuth2RestTemplate;

    //TODO вернуть Webclient
    public List<CheckUnit> getCheckUnitsByContentId(Long contentId){
        String path = "/pod/checkUnits";
        String uri = UriComponentsBuilder.fromHttpUrl(gatewayUrl).path(path).queryParam("id", contentId).build().toString();

        try {
            log.info("Получение чек-юнитов ЕРДИ {} по запросу: {}", contentId, uri);
            /*return WebClient.create()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .bodyToFlux(CheckUnit.class)
                    .collectList()
                    .block();*/
            return Arrays.asList(oAuth2RestTemplate.getForObject(uri, CheckUnit[].class));
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw AS_15_8_PPT_Exception.logAndGet(log, String.format("Ошибка получения чек-юнитов ЕРДИ %d в ППТ, код возврата %s", contentId, ex.getStatusCode()));
        }
    }

}
