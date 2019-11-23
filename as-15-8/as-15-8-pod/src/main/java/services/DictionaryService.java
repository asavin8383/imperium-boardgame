package services;

import exceptions.AS_15_8_POD_Exception;
import lombok.extern.slf4j.Slf4j;
import enums.Dictionary;
import model.projection.DictionaryView;
import model.response.PASDEntry;
import model.response.PSEntry;
import model.rest.control.ConfigPASD;
import model.rest.control.ConfigPS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import repositories.PsRepository;
import repositories.helper.DictionaryRepository;
import updaters.PASDDictionaryUpdater;
import updaters.PSDictionaryUpdater;
import utils.Utils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DictionaryService {

    private Map<Dictionary, DictionaryRepository> repositoryMap;
    private PsRepository psRepository;
    private final PSDictionaryUpdater psDictionaryUpdater;
    private final PASDDictionaryUpdater pasdDictionaryUpdater;
    private final JdbcTemplate jdbcTemplate;
    private final OAuth2RestTemplate restTemplate;

    @Value("${config.url}")
    private String configUrl;

    @Autowired
    public DictionaryService(List<DictionaryRepository> repositoryList,
                             PsRepository psRepository,
                             PSDictionaryUpdater psDictionaryUpdater,
                             JdbcTemplate jdbcTemplate,
                             OAuth2RestTemplate restTemplate,
                             PASDDictionaryUpdater pasdDictionaryUpdater) {
        this.repositoryMap = new EnumMap<>(Dictionary.class);
        for (DictionaryRepository repository : repositoryList) {
            repositoryMap.put(repository.getDictionaryType(), repository);
        }
        this.psRepository = psRepository;
        this.psDictionaryUpdater = psDictionaryUpdater;
        this.jdbcTemplate = jdbcTemplate;
        this.restTemplate = restTemplate;
        this.pasdDictionaryUpdater = pasdDictionaryUpdater;
    }

    public List<DictionaryView> getDictionaryViewList() {
        return repositoryMap.entrySet().stream()
                .map(entry -> new DictionaryView(
                        entry.getKey().toString(),
                        entry.getValue().getCountByEffDt(
                                Utils.getEndDate()),
                        entry.getKey().getShortName()))
                .collect(Collectors.toList());
    }

    public DictionaryView getDictionaryView(Dictionary dictionary) {
        DictionaryRepository repository = repositoryMap.get(dictionary);
        return new DictionaryView(dictionary.toString(), dictionary.getId(),
                dictionary.getShortName(), dictionary.getFullName(),
                repository.getUpdateDateTime(Utils.getEndDate()));
    }

    @Transactional
    public void createPs(PSEntry entry){
        if (entry.getId() != null){
            throw new AS_15_8_POD_Exception("Запрещено изменять ПС. ID = " + entry.getId());
        }

        entry.setId(getOrigId());
        entry.setDate(new Date());

        log.info("Create PS record {}", entry);
        psDictionaryUpdater.insertRecord(entry);

        ConfigPS ps = new ConfigPS(entry.Id, entry.Name, entry.Hostname);
        log.info("Sending PS record to config {}", ps);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<ConfigPS>> entity = new HttpEntity<>(Arrays.asList(ps), headers);
        restTemplate.postForObject(
                UriComponentsBuilder.fromHttpUrl(configUrl).path("/ps").build().toString(),
                entity,
                ResponseEntity.class);
    }

    @Transactional
    public void createPasd(PASDEntry entry){
        if (entry.getId() != null){
            throw new AS_15_8_POD_Exception("Запрещено изменять ПАСД. ID = " + entry.getId());
        }

        entry.setId(getOrigId());
        entry.setDate(new Date());

        log.info("Create PASD record {}", entry);
        pasdDictionaryUpdater.insertRecord(entry);

        ConfigPASD pasd = new ConfigPASD(entry.Id, entry.Name, entry.Hostname);
        log.info("Sending PASD record to config {}", pasd);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<List<ConfigPASD>> entity = new HttpEntity<>(Arrays.asList(pasd), headers);
        restTemplate.postForObject(
                UriComponentsBuilder.fromHttpUrl(configUrl).path("/pasd").build().toString(),
                entity,
                ResponseEntity.class);
    }

    @Transactional
    public void deletePs(Long id){
        log.info("Удаление ПС с ID = {}", id);
        if (id == null)
            return;

        psDictionaryUpdater.archiveRecordByOrigId(id);

        log.info("Запрос в конфиг на удаление ПС с ID = {}", id);

        restTemplate.delete(
                UriComponentsBuilder.fromHttpUrl(configUrl)
                        .path("/ps")
                        .queryParam("id", id.toString())
                        .build().toString()
                );
    }

    @Transactional
    public void deletePasd(Long id){
        log.info("Удаление ПАСД с ID = {}", id);
        if (id == null)
            return;

        pasdDictionaryUpdater.archiveRecordByOrigId(id);

        log.info("Запрос в конфиг на удаление ПАСД с ID = {}", id);

        restTemplate.delete(
                UriComponentsBuilder.fromHttpUrl(configUrl)
                        .path("/pasd")
                        .queryParam("id", id.toString())
                        .build().toString()
                );
    }

    private long getOrigId() {
        //noinspection ConstantConditions
        return -1 * jdbcTemplate.queryForObject("select nextval('sor.orig_id_seq')", Long.class);
    }

}
