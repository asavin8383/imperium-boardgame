package restapi.ppt;

import enums.ExecutionStatus;
import exceptions.AS_15_8_PPM_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.enums.ArrangementStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Slf4j
public class PptRestApi {

    private final String URI = "/ppt/arrangements/execution_status";

    @Value("${gateway.url}")
    private String gatewayUrl;

    private final OAuth2RestTemplate oAuth2RestTemplate;

    public ArrangementStatus fetchActualStatus(Long arrangementId) {
        try {
            log.debug("Отправка запроса на получение статуса мероприятия из ППТ: {}", arrangementId);
            ExecutionStatus execStatus = oAuth2RestTemplate
                    .getForObject(UriComponentsBuilder
                            .fromHttpUrl(gatewayUrl)
                            .path(URI)
                            .queryParam("id", arrangementId)
                            .build()
                            .toString(),
                        ExecutionStatus.class);
            if(execStatus == null)
                return ArrangementStatus.NEW;
            switch (execStatus){
                case SCHEDULED:
                    return ArrangementStatus.SCHEDULED;
                case RUNNING:
                    return ArrangementStatus.RUNNING;
                case STOPPING:
                    return ArrangementStatus.STOPPING;
                case STOPPED:
                    return ArrangementStatus.STOPPED;
                case FINISHED:
                case ERROR:
                case ACT_SENT:
                case CLOSED:
                    return ArrangementStatus.FINISHED;
                case NEW:
                case FORMED:
                default:
                    return ArrangementStatus.NEW;
            }
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw new AS_15_8_PPM_Exception(String.format("Ошибка отправки запроса на получение статуса мероприятия из ППТ, код возврата %s", ex.getStatusCode()));
        }
    }
}
