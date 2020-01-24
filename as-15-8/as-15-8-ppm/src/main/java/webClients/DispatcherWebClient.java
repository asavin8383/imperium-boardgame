package webClients;

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
 * Date: 23.01.2020
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DispatcherWebClient {

    private final String DISPATCHER_URI = "/dispatcher/arrangements/jobIds";

    @Value("${gateway.url}")
    private String gatewayUrl;

    public List<Long> getJobIdsFromDispatcher(Long arrangementId){
        String uri = UriComponentsBuilder.fromUriString(DISPATCHER_URI).queryParam("id", arrangementId).build().toString();
        try {
            log.info("Получение списка jobId завершенных проверок мероприятия {} по запросу: {}", arrangementId, uri);

            return WebClient.create(gatewayUrl)
                .get()
                .uri(uri)
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .flatMapMany(clientResponse -> {
                    if(clientResponse.statusCode().equals(HttpStatus.OK)){
                        log.info("Список jobId завершенных проверок мероприятия {} успешно сформирован", arrangementId);
                        return clientResponse.bodyToFlux(new ParameterizedTypeReference<Long>(){});
                    } else {
                        log.warn("Ошибка получения списка jobId завершенных проверок мероприятия {} в ППМ код возврата {}", arrangementId, clientResponse.statusCode().toString());
                        return (Flux.empty());
                    }
                })
                .collectList()
                .block();

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw AS_15_8_PPM_Exception.logAndGet(log, String.format("Ошибка получения списка jobId завершенных проверок мероприятия %d в ППМ, код возврата %s", arrangementId, ex.getStatusCode()), ex);
        } catch (Exception ex){
            throw AS_15_8_PPM_Exception.logAndGet(log, String.format("Ошибка получения списка jobId завершенных проверок мероприятия %d в ППМ", arrangementId), ex);
        }
    }
}
