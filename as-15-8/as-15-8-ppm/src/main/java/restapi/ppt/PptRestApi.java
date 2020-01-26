package restapi.ppt;

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
        log.debug("Отправка запроса на получение статуса мероприятия из ППТ: {}", arrangementId);
        try {
            String statusString = oAuth2RestTemplate
                    .getForObject(UriComponentsBuilder
                            .fromHttpUrl(gatewayUrl)
                            .path(URI)
                            .queryParam("id", arrangementId)
                            .build()
                            .toString(),
                        String.class);
            try {
                return ArrangementStatus.valueOf(statusString);
            } catch (Exception ex){
                throw new AS_15_8_PPM_Exception("Ошибка при получении актуального статуса мероприятия из ППТ. Статус не поддерживается: " + statusString);
            }

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw new AS_15_8_PPM_Exception(String.format("Ошибка отправки запроса на получение статуса мероприятия из ППТ, код возврата %s", ex.getStatusCode()));
        }
    }
}
