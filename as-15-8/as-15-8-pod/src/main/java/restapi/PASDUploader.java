package restapi;

import lombok.extern.slf4j.Slf4j;
import model.scheme.PasdRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import repositories.PasdRepository;

import java.util.List;

/**
 * User: asinjavin
 * Date: 24.10.2019
 * Time: 15:32
 */
@Service
@Slf4j
public class PASDUploader
{
    @Value("${config.url}")
    private String configUrl;

    @Value("${rep_scheduler.url}")
    private String repUrl;

    private final OAuth2RestTemplate restTemplate;

    private final PasdRepository pasdRepository;

    @Autowired
    public PASDUploader(OAuth2RestTemplate restTemplate, PasdRepository pasdRepository) {
        this.restTemplate = restTemplate;
        this.pasdRepository = pasdRepository;
    }

    public void upload() {
        List<PasdRecord> all = pasdRepository.getAllActial();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<PasdRecord>> entity = new HttpEntity<>(all, headers);

        log.info("Sending {} PASD records", all.size());
        restTemplate.postForObject(UriComponentsBuilder.fromHttpUrl(configUrl).path("/pasd").build().toString(), entity, ResponseEntity.class);
        restTemplate.postForLocation(UriComponentsBuilder.fromHttpUrl(repUrl).path("/reports/dm/refresh").build().toString(), null);
        log.info("{} PASD records sent successfully", all.size());

    }
}
