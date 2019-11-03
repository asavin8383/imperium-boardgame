package events.producers.rest;

import checkUnits.CheckUnit;
import exceptions.AS_15_8_PPM_Exception;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * Created by san
 * Date: 03.11.2019
 */
@Service
@Slf4j
public class CheckUnitUploader {

    @Value("${gateway.url}")
    private String gatewayUrl;

    public List<CheckUnit> getCheckUnitsByArrangementId(Long arrangementId){
        String path = "/ppt/arrangements/checkUnits";
        String uri = UriComponentsBuilder.fromHttpUrl(gatewayUrl).path(path).queryParam("id", arrangementId).build().toString();
        try {
            log.info("Получение чек-юнитов мероприятия {} по запросу: {}", arrangementId, uri);
            return WebClient.create()
                    .get()
                    .uri(uri)
                    .retrieve()
                    .bodyToFlux(CheckUnit.class)
                    .collectList()
                    .block();
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw AS_15_8_PPM_Exception.logAndGet(log, String.format("Ошибка получения чек-юнитов мероприятия %d в ППМ, код возврата %s", arrangementId, ex.getStatusCode()));
        }
    }
}
