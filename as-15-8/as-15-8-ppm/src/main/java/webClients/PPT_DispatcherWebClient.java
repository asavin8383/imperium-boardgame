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

/**
 * Created by san
 * Date: 03.11.2019
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class PPT_DispatcherWebClient {

    private final String PPT_CHECK_UNITS_URI = "/ppt/arrangements/checkUnits";
    private final String DISPATCHER_CHECK_UNITS_URI = "/dispatcher/arrangement/checkUnits";

    @Value("${gateway.url}")
    private String gatewayUrl;

    public List<CheckUnit> getFromPPT(Long arrangementId){
        return getCheckUnitsByArrangementId(arrangementId, PPT_CHECK_UNITS_URI);
    }

    public List<CheckUnit> getFromDispatcher(Long arrangementId){
        return getCheckUnitsByArrangementId(arrangementId, DISPATCHER_CHECK_UNITS_URI);
    }

    private List<CheckUnit> getCheckUnitsByArrangementId(Long arrangementId, String uriString){
        String uri = UriComponentsBuilder.fromUriString(uriString).queryParam("id", arrangementId).build().toString();
        try {
            log.info("Получение чек-юнитов мероприятия {} по запросу: {}", arrangementId, uri);

            return WebClient.create(gatewayUrl)
                    .get()
                    .uri(uri)
                    .accept(MediaType.TEXT_EVENT_STREAM)
                    .exchange()
                    .flatMapMany(clientResponse -> {
                        if(clientResponse.statusCode().equals(HttpStatus.OK)){
                            log.info("Check units мероприятия {} успешно сформированы", arrangementId);
                            return clientResponse.bodyToFlux(new ParameterizedTypeReference<List<CheckUnit>>(){})
                                    .flatMap(Flux::fromIterable);
                        } else {
                            log.warn("Ошибка получения чек-юнитов мероприятия {} в ППМ код возврата {}", arrangementId, clientResponse.statusCode().toString());
                            return (Flux.empty());
                        }
                    })
                    .collectList()
                    .block();

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw AS_15_8_PPM_Exception.logAndGet(log, String.format("Ошибка получения чек-юнитов мероприятия %d в ППМ, код возврата %s", arrangementId, ex.getStatusCode()), ex);
        } catch (Exception ex){
            throw AS_15_8_PPM_Exception.logAndGet(log, String.format("Ошибка получения чек-юнитов мероприятия %d в ППМ", arrangementId), ex);
        }
    }
}
