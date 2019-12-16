package webClients;

import checkUnits.CheckUnit;
import exceptions.AS_15_8_PPM_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by san
 * Date: 03.11.2019
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PptWebClient {

    private final String CHECK_UNITS_URI = "/ppt/arrangements/checkUnits";

    @Value("${gateway.url}")
    private String gatewayUrl;

    /*public List<CheckUnit> getCheckUnitsByArrangementId(Long arrangementId){
        String uri = UriComponentsBuilder.fromUriString(CHECK_UNITS_URI).queryParam("id", arrangementId).build().toString();
        try {
            log.info("Получение чек-юнитов мероприятия {} по запросу: {}", arrangementId, uri);

            List<CheckUnit> checkUnits = WebClient.create(gatewayUrl)
                    .get()
                    .uri(uri)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .retrieve()
                    .bodyToFlux(CheckUnit.class)
                    .collectList()
                    .block();
            log.info("Check units мероприятия {} успешно сформированы", arrangementId, uri);
            return checkUnits;
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw AS_15_8_PPM_Exception.logAndGet(log, String.format("Ошибка получения чек-юнитов мероприятия %d в ППМ, код возврата %s", arrangementId, ex.getStatusCode()), ex);
        } catch (Exception ex){
            throw AS_15_8_PPM_Exception.logAndGet(log, String.format("Ошибка получения чек-юнитов мероприятия %d в ППМ", arrangementId), ex);
        }
    }*/

    public Flux<List<CheckUnit>> getCheckUnitsByArrangementId(Long arrangementId){
        String uri = UriComponentsBuilder.fromUriString(CHECK_UNITS_URI).queryParam("id", arrangementId).build().toString();
        try {
            log.info("Получение чек-юнитов мероприятия {} по запросу: {}", arrangementId, uri);

            WebClient.create(gatewayUrl)
                    .get()
                    .uri(uri)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .exchange()
                    .flatMapMany(clientResponse -> {
                        if(clientResponse.statusCode().equals(HttpStatus.OK)){
                            log.info("Check units мероприятия {} успешно сформированы", arrangementId, uri);
                           return clientResponse.bodyToFlux(CheckUnit.class);
                        } else {
                            log.info("Ошибка получения чек-юнитов мероприятия %d в ППМ", arrangementId, uri);
                            return (Flux.empty());
                        }
                    });

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw AS_15_8_PPM_Exception.logAndGet(log, String.format("Ошибка получения чек-юнитов мероприятия %d в ППМ, код возврата %s", arrangementId, ex.getStatusCode()), ex);
        } catch (Exception ex){
            throw AS_15_8_PPM_Exception.logAndGet(log, String.format("Ошибка получения чек-юнитов мероприятия %d в ППМ", arrangementId), ex);
        }
        return Flux.empty();
    }
}
