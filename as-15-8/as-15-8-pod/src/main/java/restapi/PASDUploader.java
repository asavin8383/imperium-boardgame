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

    private final OAuth2RestTemplate oauth2RestTemplate;

    private final PasdRepository pasdRepository;

    @Autowired
    public PASDUploader(OAuth2RestTemplate oauth2RestTemplate, PasdRepository pasdRepository) {
        this.oauth2RestTemplate = oauth2RestTemplate;
        this.pasdRepository = pasdRepository;
    }

    public void upload() {
        List<PasdRecord> all = pasdRepository.getAllActial();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<PasdRecord>> entity = new HttpEntity<>(all, headers);

        log.debug("Sending {} PASD records", all.size());
        oauth2RestTemplate.postForObject(UriComponentsBuilder.fromHttpUrl(configUrl).path("/config/pasd").build().toString(), entity, ResponseEntity.class);
        log.debug("{} PASD records sent successfully", all.size());

    }
}
