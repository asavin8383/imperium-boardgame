package restapi;

import lombok.extern.slf4j.Slf4j;
import model.response.PSEntry;
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
public class PSRestClient
{
    @Autowired
    PSDictionaryUpdater psDictionaryUpdater;

    @Autowired
    RestTemplate restTemplate;

    @Value("${spring.rest_base_url}")
    private String baseUrl;


    public void readFromNet() {
        String url = baseUrl + "getPSList/";
        ResponseEntity<RestResponsePS> entity = restTemplate.getForEntity(url, RestResponsePS.class);

        RestResponsePS resp = entity.getBody();

        List<PSEntry> list = resp.getListPSEntry();

        log.info("Got PS records: {}", list.toString());
        psDictionaryUpdater.insertRecords(list);

    }
}
