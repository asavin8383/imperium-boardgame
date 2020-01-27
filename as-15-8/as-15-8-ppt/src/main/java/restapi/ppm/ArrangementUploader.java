package restapi.ppm;

import arrangement.ArrangementToPPM;
import exceptions.AS_15_8_PPT_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.task.Arrangement;
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
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Created by san
 * Date: 29.10.2019
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ArrangementUploader {

    private final String ARRANGEMENTS_URI = "/ppm/arrangements";

    @Value("${gateway.url}")
    private String gatewayUrl;

    private final OAuth2RestTemplate oAuth2RestTemplate;

    public void updateArrangement(Arrangement arrangement){
        ArrangementToPPM arrToPPM = convertToArrangementPPM(arrangement);

        HttpEntity<MappingJacksonValue> entity = createHttpEntity(arrToPPM);

        //TODO сделать POJO для межсервисного обмена. В случае 400 ошибки - смотреть тело
        log.info("Отправка мероприятия в ППМ: {}", arrangement.getId());
        try {
            oAuth2RestTemplate.put(UriComponentsBuilder.fromHttpUrl(gatewayUrl).path(ARRANGEMENTS_URI).queryParam("id", arrangement.getId()).build().toString(), entity);
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
             throw AS_15_8_PPT_Exception.logAndGet(log, String.format("Ошибка отправки мероприятия %d в ППМ, код возврата %s", arrangement.getId(), ex.getStatusCode()), ex);
        }
        log.info("Мероприятие {} успешно отправлено в ППМ", arrangement.getId());
    }

    private ArrangementToPPM convertToArrangementPPM(Arrangement arrangement) {
        ArrangementToPPM arrangementToPPM = new ArrangementToPPM();
        arrangementToPPM.setId(arrangement.getId());
        arrangementToPPM.setTitle(arrangement.getTitle());
        arrangementToPPM.setCreationDate(arrangement.getCreationDate());
        arrangementToPPM.setPlannedStartTime(arrangement.getPlannedStartTime());
        arrangementToPPM.setPlannedEndTime(arrangement.getPlannedEndTime());
        arrangementToPPM.setMaxWorkersCount(arrangement.getMaxWorkersCount());
        arrangementToPPM.setAccessTool(arrangement.getAccessTool());
        return arrangementToPPM;
    }

    private HttpEntity createHttpEntity(ArrangementToPPM arrangementToPPM) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        MappingJacksonValue jacksonValue = new MappingJacksonValue(arrangementToPPM);
        HttpEntity<MappingJacksonValue> entity = new HttpEntity<>(jacksonValue, headers);

        return entity;
    }
}
