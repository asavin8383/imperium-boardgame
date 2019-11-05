package restapi;

import lombok.extern.slf4j.Slf4j;
import model.scheme.PsRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import repositories.PsRepository;

import java.util.List;

/**
 * User: asinjavin
 * Date: 24.10.2019
 * Time: 15:32
 */
@Service
@Slf4j
public class PSUploader
{
    @Value("${config.url}")
    private String configUrl;

    @Value("${rep_scheduler.url}")
    private String repUrl;

    private final OAuth2RestTemplate restTemplate;

    private final PsRepository psRepository;

    @Autowired
    public PSUploader(OAuth2RestTemplate restTemplate, PsRepository psRepository) {this.restTemplate = restTemplate;
        this.psRepository = psRepository;
    }

    public void upload() {
        List<PsRecord> all = psRepository.getAllActial();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<PsRecord>> entity = new HttpEntity<>(all, headers);

        log.info("Sending {} PS records", all.size());
        restTemplate.postForObject(UriComponentsBuilder.fromHttpUrl(configUrl).path("/ps").build().toString(), entity, ResponseEntity.class);
        restTemplate.postForLocation(UriComponentsBuilder.fromHttpUrl(repUrl).path("/reports/dm/refresh").build().toString(), null);
        log.info("{} PS records sent successfully", all.size());

    }
}
