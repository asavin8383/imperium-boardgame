package restapi;

import lombok.extern.slf4j.Slf4j;
import model.response.PASDEntry;
import model.response.PSEntry;
import model.response.RestResponsePASD;
import model.response.RestResponsePS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

/**
 * User: asinjavin
 * Date: 17.10.2019
 * Time: 15:43
 */
@Component
@Slf4j
public class PASDRestClient
{
    @Autowired
    PASDDictionaryUpdater pasdDictionaryUpdater;

    @Autowired
    RestTemplate restTemplate;

    @Value("${spring.rest_base_url}")
    private String baseUrl;


    public void readFromNet() {
        String base = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        String url = base + "getPASDList/";
        log.info("GET from {}", url);
        ResponseEntity<RestResponsePASD> entity = restTemplate.getForEntity(url, RestResponsePASD.class);

        RestResponsePASD resp = entity.getBody();

        List<PASDEntry> list = resp.getListPSEntry();

        log.info("Got PS records: {}", list.toString());
        pasdDictionaryUpdater.insertRecords(list);

    }
}
