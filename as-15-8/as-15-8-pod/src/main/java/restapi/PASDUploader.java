package restapi;

import lombok.extern.slf4j.Slf4j;
import model.scheme.PasdRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
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
    @Value("${gateway.url}")
    private String gatewayUrl;

    @Autowired @Qualifier("internal")
    RestTemplate restTemplate;

    @Autowired
    PasdRepository pasdRepository;

    public void upload() {
        List<PasdRecord> all = pasdRepository.getAllActial();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<List<PasdRecord>> entity = new HttpEntity<>(all, headers);

        log.debug("Sending {} PS records", all.size());
        restTemplate.postForObject(gatewayUrl, entity, ResponseEntity.class);
        log.debug("{} PS records sent successfully", all.size());

    }
}
