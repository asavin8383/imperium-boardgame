package webClients;

import checkUnits.CheckUnit;
import exceptions.AS_15_8_PPM_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.logging.Level;

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

    public List<CheckUnit> getCheckUnitsByArrangementId(Long arrangementId){
        String uri = UriComponentsBuilder.fromUriString(CHECK_UNITS_URI).queryParam("id", arrangementId).build().toString();
        try {
            log.info("Получение чек-юнитов мероприятия {} по запросу: {}", arrangementId, uri);
            return WebClient.create(gatewayUrl)
                    .get()
                    .uri(uri)
                    .retrieve()
                    .bodyToFlux(CheckUnit.class)
                    .log("checkUnits", Level.INFO)
                    .collectList()
                    .block();
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw AS_15_8_PPM_Exception.logAndGet(log, String.format("Ошибка получения чек-юнитов мероприятия %d в ППМ, код возврата %s", arrangementId, ex.getStatusCode()), ex);
        }
    }

}
