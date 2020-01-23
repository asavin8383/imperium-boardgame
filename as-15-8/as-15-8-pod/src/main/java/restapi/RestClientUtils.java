package restapi;

import com.fasterxml.jackson.databind.ObjectMapper;
import exceptions.AS_15_8_POD_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import model.response.DeltaIdEntry;
import model.response.RestResponseDeltaListByDate;
import model.response.RestResponseStatusString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class RestClientUtils {

    private final RestTemplate registryAnonimyzersRestTemplate;

    @Value("${spring.rest_base_url}")
    private String baseUrl;


    public boolean checkDeltaErdiNotFound(Path path){
        try {
            File f = path.toFile();
            if (f.length() >= 2048)
                return false;

            String content = new String(Files.readAllBytes(path));
            RestResponseStatusString responseStatus = RestResponseStatusString.getFromData(content);
            return responseStatus != null &&
                    "Not found".equalsIgnoreCase(responseStatus.response);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean checkDeltaErdiNotFound(byte[] content){
        if (content == null)
            return true;

        if (content.length >= 2048)
            return false;

        try {
            RestResponseStatusString responseStatus = RestResponseStatusString.getFromData(content);
            return responseStatus != null &&
                    "Not found".equalsIgnoreCase(responseStatus.response);
        }
        catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Загрузка списка дельт ЕРДИ по дате. Парамтер data
     */
    public List<DeltaIdEntry> getDumpDeltaListByDate(Date date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        //dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+3"));
        String dateStr = dateFormat.format(date);

        UriComponents uriComponents =
                UriComponentsBuilder.fromUriString("{baseUrl}/getDumpDeltaListByDate/{dateStr}/")
                        .build()
                        .expand(baseUrl, dateStr);

        System.out.println(uriComponents.toString());

        RestResponseDeltaListByDate resp = null;
        try {
            log.info("Получение списка дельт: {} по дате: {}", uriComponents.toString(), dateStr);
            String entityString = registryAnonimyzersRestTemplate
                    .getForObject(uriComponents.toString(), String.class);

            RestResponseStatusString responseStatus = RestResponseStatusString.getFromData(entityString);
            if (responseStatus != null){
                throw new AS_15_8_POD_Exception("Невозможно получить спиосок дельт! Ответ от ППП: " + responseStatus.toString());
            }

            resp = new ObjectMapper()
                    .readValue(entityString, RestResponseDeltaListByDate.class);
        }
        catch (Throwable e){
            log.error("Ошибка загрузки списка дельт по дате: {}", dateStr);
            log.error("Текст ошибки", e);
            throw (e instanceof AS_15_8_POD_Exception ?
                    (AS_15_8_POD_Exception)e :
                    new AS_15_8_POD_Exception("Ошибка загрузки списка дельт по дате " + dateStr, e));
        }

        List<DeltaIdEntry> list = resp.response;
        list = list.stream()
                .filter(deltaIdEntry -> !deltaIdEntry.isEmpty)
                .collect(Collectors.toList());
        return list;
    }

}
