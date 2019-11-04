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

    private final OAuth2RestTemplate oauth2RestTemplate;

    private final PsRepository psRepository;

    @Autowired
    public PSUploader(OAuth2RestTemplate oauth2RestTemplate, PsRepository psRepository) {this.oauth2RestTemplate = oauth2RestTemplate;
        this.psRepository = psRepository;
    }

    public void upload() {
        List<PsRecord> all = psRepository.getAllActial();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<PsRecord>> entity = new HttpEntity<>(all, headers);

        log.debug("Sending {} PS records", all.size());
        oauth2RestTemplate.postForObject(UriComponentsBuilder.fromHttpUrl(configUrl).path("/config/ps").build().toString(), entity, ResponseEntity.class);
        log.debug("{} PS records sent successfully", all.size());

    }
}
