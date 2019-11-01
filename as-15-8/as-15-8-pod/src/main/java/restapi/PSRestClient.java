package restapi;

import lombok.extern.slf4j.Slf4j;
import model.response.PSEntry;
import model.response.RestResponsePS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import restapi.updaters.PSDictionaryUpdater;

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
    RestTemplate registryAnonimyzersRestTemplate;

    @Value("${spring.rest_base_url}")
    private String baseUrl;

    @Autowired
    private PSUploader psUploader;


    public void readFromNet() {
        String base = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        String url = base + "getPSList/";
        log.info("GET from {}", url);
        ResponseEntity<RestResponsePS> entity = registryAnonimyzersRestTemplate.getForEntity(url, RestResponsePS.class);

        RestResponsePS resp = entity.getBody();

        List<PSEntry> list = resp.getListPSEntry();

        log.info("Got PS records: {}", list.toString());
        psDictionaryUpdater.insertRecords(list);

        psUploader.upload();
    }
}
