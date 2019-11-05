package restapi;

import lombok.extern.slf4j.Slf4j;
import model.response.PASDEntry;
import model.response.RestResponsePASD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import updaters.PASDDictionaryUpdater;

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
    @Value("${spring.rest_base_url}")
    private String baseUrl;

    private final PASDDictionaryUpdater pasdDictionaryUpdater;

    private final RestTemplate registryAnonimyzersRestTemplate;

    private final PASDUploader pasdUploader;

    @Autowired
    public PASDRestClient(PASDDictionaryUpdater pasdDictionaryUpdater, RestTemplate registryAnonimyzersRestTemplate, PASDUploader pasdUploader) {
        this.pasdDictionaryUpdater = pasdDictionaryUpdater;
        this.registryAnonimyzersRestTemplate = registryAnonimyzersRestTemplate;
        this.pasdUploader = pasdUploader;
    }


    public void readFromNet() {
        String base = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        String url = base + "getPASDList/";
        log.info("GET from {}", url);
        ResponseEntity<RestResponsePASD> entity = registryAnonimyzersRestTemplate.getForEntity(url, RestResponsePASD.class);

        RestResponsePASD resp = entity.getBody();

        List<PASDEntry> list = resp.getListPSEntry();

        log.info("Got PS records: {}", list.toString());
        pasdDictionaryUpdater.insertRecords(list);
        pasdUploader.upload();

    }
}
