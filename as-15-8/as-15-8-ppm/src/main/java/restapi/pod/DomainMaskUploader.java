package restapi.pod;

import exceptions.AS_15_8_PPM_Exception;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by san
 * Date: 13.11.2019
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class DomainMaskUploader {

    private final String URI = "/pod/domain-masks";

    @Value("${gateway.url}")
    private String gatewayUrl;

    private final OAuth2RestTemplate oAuth2RestTemplate;

    public List<String> getDomains(String domainMask){
        log.info("Отправка запроса на получение доменов по маске в ПОД: {}", domainMask);
        try {
            List<String> domains = new ArrayList<>();
            String[] queryResult = oAuth2RestTemplate.getForObject(UriComponentsBuilder.fromHttpUrl(gatewayUrl).path(URI).queryParam("mask", domainMask).build().toString(), String[].class);
            if(queryResult != null){
                domains = Arrays.asList(queryResult);
            }
            log.info("Из ПОД получено {} элементов для маски: {}", domains.size(), domainMask);
            return domains;
        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            throw AS_15_8_PPM_Exception.logAndGet(log, String.format("Ошибка отправки запроса на получение доменов по маске, код возврата %s", ex.getStatusCode()));
        }
    }
}
